package javaapplication1.erp.ui.screens;

import javaapplication1.erp.service.AuthManager;
import javaapplication1.erp.ui.components.RoundedButton;
import javaapplication1.erp.ui.components.RoundedTextField;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.WindowUtil;

import java.awt.BorderLayout;
import java.awt.Color;
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
 * Forces user to replace temporary password.
 */
public class ChangePasswordDialog extends Dialog {

    private final String username;
    private final String currentPassword;
    private final RoundedTextField newPasswordField = new RoundedTextField("New password", true);
    private final RoundedTextField confirmPasswordField = new RoundedTextField("Confirm new password", true);
    private final Label status = new Label("", Label.CENTER);

    public ChangePasswordDialog(Frame owner, String username, String currentPassword, Runnable onChanged) {
        super(owner, "Change Password", true);
        this.username = username;
        this.currentPassword = currentPassword;
        setSize(480, 320);
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        WindowUtil.centerWindow(this);

        Panel root = new Panel(new GridBagLayout());
        root.setBackground(Theme.BACKGROUND);
        add(root, BorderLayout.CENTER);

        Panel card = new Panel(new GridBagLayout());
        card.setBackground(Theme.SURFACE);
        card.setPreferredSize(new java.awt.Dimension(420, 240));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 24, 8, 24);

        Label title = new Label("Temporary password detected", Label.CENTER);
        title.setFont(Theme.FONT_SUBHEADING);
        title.setForeground(Theme.PRIMARY_BLUE);
        c.gridy = 0;
        card.add(title, c);

        Label hint = new Label("Set a permanent password to continue.", Label.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        c.gridy = 1;
        card.add(hint, c);

        c.insets = new Insets(12, 24, 8, 24);
        c.gridy = 2;
        card.add(newPasswordField, c);
        c.insets = new Insets(0, 24, 8, 24);
        c.gridy = 3;
        card.add(confirmPasswordField, c);

        status.setFont(Theme.FONT_SMALL);
        status.setForeground(Theme.ERROR_RED);
        c.gridy = 4;
        card.add(status, c);

        Panel actions = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setBackground(Theme.SURFACE);
        RoundedButton submit = new RoundedButton("Update Password", () -> submit(onChanged));
        submit.setPreferredSize(new java.awt.Dimension(180, Theme.BUTTON_HEIGHT));
        actions.add(submit);
        c.gridy = 5;
        card.add(actions, c);

        root.add(card);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void submit(Runnable onChanged) {
        String np = newPasswordField.getText();
        String cp = confirmPasswordField.getText();
        if (np == null || np.trim().length() < 4) {
            status.setText("Password must be at least 4 characters.");
            return;
        }
        if (!np.equals(cp)) {
            status.setText("Passwords do not match.");
            return;
        }
        AuthManager.RequestResult result = AuthManager.changePasswordAfterTempLogin(username, currentPassword, np);
        status.setForeground(result.success ? new Color(0x2E7D32) : Theme.ERROR_RED);
        status.setText(result.message);
        if (result.success) {
            if (onChanged != null) {
                onChanged.run();
            }
            dispose();
        }
    }
}
