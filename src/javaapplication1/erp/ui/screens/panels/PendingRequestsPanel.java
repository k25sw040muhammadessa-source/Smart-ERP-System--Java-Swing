package javaapplication1.erp.ui.screens.panels;

import javaapplication1.erp.service.AuthManager;
import javaapplication1.erp.ui.components.RoundedButton;
import javaapplication1.erp.ui.components.RoundedTextField;
import javaapplication1.erp.ui.theme.Theme;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.util.List;

/**
 * Admin screen for approving/rejecting password reset requests and signup approvals.
 */
public class PendingRequestsPanel extends Panel {

    private final Choice requestTypeChoice = new Choice();
    private final Choice requestChoice = new Choice();
    private final RoundedTextField approverField = new RoundedTextField("Approver username");
    private final RoundedTextField approverPasswordField = new RoundedTextField("Approver password", true);
    private final RoundedTextField rejectionReasonField = new RoundedTextField("Reject reason (optional)");
    private final Label detailsLabel = new Label("Select a request to see details.");
    private final Label statusLabel = new Label("");
    private List<AuthManager.ResetRequest> cachedResetRequests;
    private List<AuthManager.SignupRequest> cachedSignupRequests;

    public PendingRequestsPanel() {
        setLayout(new BorderLayout(0, Theme.SPACING_16));
        setBackground(Theme.SURFACE);
        approverField.setPreferredSize(new java.awt.Dimension(460, Theme.INPUT_HEIGHT));
        approverPasswordField.setPreferredSize(new java.awt.Dimension(460, Theme.INPUT_HEIGHT));
        rejectionReasonField.setPreferredSize(new java.awt.Dimension(460, Theme.INPUT_HEIGHT));

        Label title = new Label("Pending Requests");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        Panel body = new Panel(new GridLayout(0, 1, 0, Theme.SPACING_12));
        body.setBackground(Theme.SURFACE);

        requestTypeChoice.add("Password Reset Requests");
        requestTypeChoice.add("Signup Approvals");
        body.add(labeled("Request type", requestTypeChoice));

        body.add(requestChoice);
        detailsLabel.setForeground(Theme.TEXT_MUTED);
        body.add(detailsLabel);

        approverField.setText("admin");
        body.add(labeled("Approver username", approverField));
        body.add(labeled("Approver password", approverPasswordField));
        body.add(labeled("Reject reason (optional)", rejectionReasonField));

        Panel actions = new Panel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING_12, 0));
        actions.setBackground(Theme.SURFACE);
        RoundedButton approveButton = new RoundedButton("Approve", this::approveSelected);
        approveButton.setPreferredSize(new java.awt.Dimension(160, Theme.BUTTON_HEIGHT));
        RoundedButton rejectButton = new RoundedButton("Reject", this::rejectSelected);
        rejectButton.setPreferredSize(new java.awt.Dimension(130, Theme.BUTTON_HEIGHT));
        rejectButton.setColors(new Color(0xE84E40), new Color(0xD73C2D), new Color(0xBF3325));
        actions.add(approveButton);
        actions.add(rejectButton);
        body.add(actions);

        statusLabel.setForeground(Theme.TEXT_MUTED);
        body.add(statusLabel);

        add(body, BorderLayout.CENTER);
        requestTypeChoice.addItemListener(e -> refreshData());
        requestChoice.addItemListener(e -> updateDetails());
        refreshData();
    }

    public void refreshData() {
        if (requestTypeChoice.getSelectedIndex() == 1) {
            loadSignupRequests();
            return;
        }

        loadResetRequests();
    }

    private void loadResetRequests() {
        cachedResetRequests = AuthManager.getResetRequests();
        cachedSignupRequests = null;
        requestChoice.removeAll();
        for (AuthManager.ResetRequest request : cachedResetRequests) {
            requestChoice.add(request.id + " | " + request.account + " | " + request.status);
        }
        if (requestChoice.getItemCount() == 0) {
            detailsLabel.setText("No requests available.");
        } else {
            requestChoice.select(0);
            updateDetails();
        }
    }

    private void loadSignupRequests() {
        cachedSignupRequests = AuthManager.getPendingSignupRequests();
        cachedResetRequests = null;
        requestChoice.removeAll();
        for (AuthManager.SignupRequest request : cachedSignupRequests) {
            requestChoice.add(request.username + " | " + request.email + " | Pending");
        }
        if (requestChoice.getItemCount() == 0) {
            detailsLabel.setText("No signup approvals available.");
        } else {
            requestChoice.select(0);
            updateDetails();
        }
    }

    private void updateDetails() {
        int idx = requestChoice.getSelectedIndex();
        if (requestTypeChoice.getSelectedIndex() == 1) {
            if (cachedSignupRequests == null || idx < 0 || idx >= cachedSignupRequests.size()) {
                detailsLabel.setText("No signup selected.");
                return;
            }
            AuthManager.SignupRequest request = cachedSignupRequests.get(idx);
            detailsLabel.setText("Email: " + request.email + " | Role: User | Status: Pending approval");
            return;
        }

        if (cachedResetRequests == null || idx < 0 || idx >= cachedResetRequests.size()) {
            detailsLabel.setText("No request selected.");
            return;
        }
        AuthManager.ResetRequest request = cachedResetRequests.get(idx);
        detailsLabel.setText("Reason: " + request.reason + " | Submitted: " + request.submittedAt + " | Note: " + request.note);
    }

    private void approveSelected() {
        int idx = requestChoice.getSelectedIndex();
        if (requestTypeChoice.getSelectedIndex() == 1) {
            if (cachedSignupRequests == null || idx < 0 || idx >= cachedSignupRequests.size()) {
                statusLabel.setForeground(Theme.ERROR_RED);
                statusLabel.setText("Select signup request first.");
                return;
            }
            AuthManager.SignupRequest request = cachedSignupRequests.get(idx);
            AuthManager.RequestResult result = AuthManager.approveRegisteredUser(
                    request.username,
                    approverField.getText(),
                    approverPasswordField.getText()
            );
            statusLabel.setForeground(result.success ? new Color(0x2E7D32) : Theme.ERROR_RED);
            statusLabel.setText(result.message);
            refreshData();
            return;
        }

        if (cachedResetRequests == null || idx < 0 || idx >= cachedResetRequests.size()) {
            statusLabel.setForeground(Theme.ERROR_RED);
            statusLabel.setText("Select request first.");
            return;
        }
        AuthManager.ResetRequest request = cachedResetRequests.get(idx);
        AuthManager.RequestResult result = AuthManager.approveResetRequest(request.id, approverField.getText(), approverPasswordField.getText());
        statusLabel.setForeground(result.success ? new Color(0x2E7D32) : Theme.ERROR_RED);
        statusLabel.setText(result.message);
        refreshData();
    }

    private void rejectSelected() {
        int idx = requestChoice.getSelectedIndex();
        if (requestTypeChoice.getSelectedIndex() == 1) {
            if (cachedSignupRequests == null || idx < 0 || idx >= cachedSignupRequests.size()) {
                statusLabel.setForeground(Theme.ERROR_RED);
                statusLabel.setText("Select signup request first.");
                return;
            }
            AuthManager.SignupRequest request = cachedSignupRequests.get(idx);
            AuthManager.RequestResult result = AuthManager.rejectRegisteredUser(
                    request.username,
                    approverField.getText(),
                    approverPasswordField.getText(),
                    rejectionReasonField.getText()
            );
            statusLabel.setForeground(result.success ? new Color(0x2E7D32) : Theme.ERROR_RED);
            statusLabel.setText(result.message);
            refreshData();
            return;
        }

        if (cachedResetRequests == null || idx < 0 || idx >= cachedResetRequests.size()) {
            statusLabel.setForeground(Theme.ERROR_RED);
            statusLabel.setText("Select request first.");
            return;
        }
        AuthManager.ResetRequest request = cachedResetRequests.get(idx);
        AuthManager.RequestResult result = AuthManager.rejectResetRequest(
                request.id,
                approverField.getText(),
                approverPasswordField.getText(),
                rejectionReasonField.getText()
        );
        statusLabel.setForeground(result.success ? new Color(0x2E7D32) : Theme.ERROR_RED);
        statusLabel.setText(result.message);
        refreshData();
    }

    private Panel labeled(String label, java.awt.Component comp) {
        Panel panel = new Panel(new BorderLayout(0, 4));
        panel.setBackground(Theme.SURFACE);
        Label l = new Label(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        panel.add(l, BorderLayout.NORTH);
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }
}
