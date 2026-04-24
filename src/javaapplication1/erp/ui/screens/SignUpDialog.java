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

public class SignUpDialog extends Dialog {

    private final RoundedTextField username = new RoundedTextField("Username");
    private final RoundedTextField email = new RoundedTextField("Email");
    private final RoundedTextField password = new RoundedTextField("Password", true);
    private final RoundedTextField confirm = new RoundedTextField("Confirm password", true);
    private final Label status = new Label("", Label.CENTER);

    public SignUpDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Create Account", true);
        setSize(520, 430);
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        WindowUtil.centerWindow(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        Panel root = new Panel(new GridBagLayout());
        root.setBackground(Theme.BACKGROUND);
        add(root, BorderLayout.CENTER);

        Panel card = new Panel(new GridBagLayout());
        card.setBackground(Theme.SURFACE);
        card.setPreferredSize(new java.awt.Dimension(460, 360));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 24, 8, 24);

        Label title = new Label("Create new account", Label.CENTER);
        title.setForeground(Theme.PRIMARY_BLUE);
        title.setFont(Theme.FONT_HEADING);
        c.gridy = 0;
        card.add(title, c);

        Label notice = new Label("New accounts stay pending until an admin approves them.", Label.CENTER);
        notice.setForeground(Theme.TEXT_MUTED);
        notice.setFont(Theme.FONT_SMALL);
        c.gridy = 1;
        c.insets = new Insets(0, 24, 8, 24);
        card.add(notice, c);

        c.gridy = 2;
        card.add(username, c);
        c.gridy = 3;
        card.add(email, c);
        c.gridy = 4;
        card.add(password, c);
        c.gridy = 5;
        card.add(confirm, c);

        status.setFont(Theme.FONT_SMALL);
        status.setForeground(Theme.ERROR_RED);
        c.gridy = 6;
        card.add(status, c);

        Panel actions = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setBackground(Theme.SURFACE);
        RoundedButton create = new RoundedButton("Create Account", () -> submit(onSuccess));
        create.setPreferredSize(new java.awt.Dimension(190, Theme.BUTTON_HEIGHT));
        actions.add(create);
        c.gridy = 7;
        c.insets = new Insets(12, 24, 12, 24);
        card.add(actions, c);

        root.add(card);
    }

    private void submit(Runnable onSuccess) {
        if (!password.getText().equals(confirm.getText())) {
            status.setText("Passwords do not match.");
            return;
        }
        AuthManager.RequestResult result = AuthManager.registerUser(username.getText(), email.getText(), password.getText());
        status.setForeground(result.success ? new Color(0x2E7D32) : Theme.ERROR_RED);
        status.setText(result.message);
        if (result.success && onSuccess != null) {
            onSuccess.run();
            dispose();
        }
    }
}
