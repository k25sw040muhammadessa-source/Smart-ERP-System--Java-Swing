package javaapplication1.erp.ui.screens;

import javaapplication1.erp.service.AuthManager;
import javaapplication1.erp.ui.components.RoundedButton;
import javaapplication1.erp.ui.components.RoundedTextField;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.WindowUtil;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Admin-approved password reset dialog.
 */
public class ForgotPasswordDialog extends Dialog {

    private final RoundedTextField accountField = new RoundedTextField("Account to reset (admin)");
    private final RoundedTextField reasonField = new RoundedTextField("Reason for reset");
    private final Label statusLabel = new Label("", Label.CENTER);

    public ForgotPasswordDialog(Frame owner, Runnable onResetSuccess) {
        super(owner, "Forgot Password - Admin Approval", true);
        setSize(560, 500);
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        WindowUtil.centerWindow(this);

        Panel container = new Panel(new GridBagLayout());
        container.setBackground(Theme.BACKGROUND);
        add(container, BorderLayout.CENTER);

        Panel card = new Panel(new GridBagLayout());
        card.setBackground(Theme.SURFACE);
        card.setPreferredSize(new java.awt.Dimension(500, 420));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 24, 8, 24);

        Label title = new Label("Password Reset Request", Label.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.PRIMARY_BLUE);
        c.gridy = 0;
        card.add(title, c);

        Label desc = new Label("Submit request. Admin will review from dashboard.", Label.CENTER);
        desc.setFont(Theme.FONT_SMALL);
        desc.setForeground(Theme.TEXT_MUTED);
        c.gridy = 1;
        card.add(desc, c);

        c.insets = new Insets(12, 24, 8, 24);
        c.gridy = 2;
        card.add(accountField, c);
        c.insets = new Insets(0, 24, 8, 24);
        c.gridy = 3;
        card.add(reasonField, c);

        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.ERROR_RED);
        c.gridy = 4;
        c.insets = new Insets(0, 24, 8, 24);
        card.add(statusLabel, c);

        Panel buttonRow = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setBackground(Theme.SURFACE);
        RoundedButton cancel = new RoundedButton("Cancel", this::dispose);
        cancel.setPreferredSize(new java.awt.Dimension(130, Theme.BUTTON_HEIGHT));
        cancel.setColors(new java.awt.Color(0xE4E6EB), new java.awt.Color(0xD8DBE1), new java.awt.Color(0xC5C9D2));
        cancel.setTextColor(Theme.TEXT_PRIMARY);
        RoundedButton approve = new RoundedButton("Submit Request", () -> submit(onResetSuccess));
        approve.setPreferredSize(new java.awt.Dimension(220, Theme.BUTTON_HEIGHT));
        buttonRow.add(cancel);
        buttonRow.add(approve);
        c.gridy = 5;
        c.insets = new Insets(8, 24, 16, 24);
        card.add(buttonRow, c);

        GridBagConstraints wrap = new GridBagConstraints();
        wrap.gridx = 0;
        wrap.gridy = 0;
        wrap.anchor = GridBagConstraints.CENTER;
        container.add(card, wrap);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void submit(Runnable onResetSuccess) {
        String account = accountField.getText().trim();
        String reason = reasonField.getText().trim();
        if (account.isEmpty()) {
            statusLabel.setForeground(Theme.ERROR_RED);
            statusLabel.setText("Account is required.");
            return;
        }
        AuthManager.RequestResult result = AuthManager.submitResetRequest(account, reason);
        statusLabel.setForeground(result.success ? new java.awt.Color(0x2E7D32) : Theme.ERROR_RED);
        statusLabel.setText(result.message);
        if (result.success && onResetSuccess != null) {
            onResetSuccess.run();
        }
    }
}
