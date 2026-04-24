package javaapplication1.erp.ui;

import javaapplication1.erp.dao.impl.UserDAOImpl;
import javaapplication1.erp.model.User;
import javaapplication1.erp.service.AuthService;
import javaapplication1.erp.util.UserSession;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.AnimatedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * LoginForm: modal dialog to authenticate user with modern centered design.
 */
public class LoginForm extends JDialog {
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JButton btnLogin = new JButton("Login");
    private final AuthService authService;

    public LoginForm(Frame parent) {
        super(parent, "Login", true);
        this.authService = new AuthService(new UserDAOImpl());
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Outer container for centering
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(Theme.BACKGROUND);
        
        // Animated content panel
        AnimatedPanel animatedContainer = new AnimatedPanel(new BorderLayout());
        animatedContainer.setPreferredSize(new Dimension(430, 300));
        animatedContainer.setMaximumSize(new Dimension(430, 300));
        animatedContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Inner content panel with vertical layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(null);
        contentPanel.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Please enter your credentials to continue");
        subtitleLabel.setFont(Theme.FONT_SMALL);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(null);
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(350, 120));

        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(Theme.FONT_BODY_MEDIUM);
        usernameLabel.setForeground(Theme.TEXT_PRIMARY);
        formPanel.add(usernameLabel);
        txtUsername.setFont(Theme.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(300, 36));
        txtUsername.setMaximumSize(new Dimension(300, 36));
        txtUsername.setToolTipText("e.g. user123");
        txtUsername.setText("");
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(12));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(Theme.FONT_BODY_MEDIUM);
        passwordLabel.setForeground(Theme.TEXT_PRIMARY);
        formPanel.add(passwordLabel);
        txtPassword.setFont(Theme.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(300, 36));
        txtPassword.setMaximumSize(new Dimension(300, 36));
        txtPassword.setToolTipText("Enter your password");
        txtPassword.setText("");
        formPanel.add(txtPassword);

        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(24));

        // Button
        btnLogin.setFont(Theme.FONT_BODY_MEDIUM);
        btnLogin.setBackground(Theme.PRIMARY_BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setPreferredSize(new Dimension(120, 40));
        btnLogin.setMaximumSize(new Dimension(120, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(null);
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(btnLogin);
        buttonPanel.add(Box.createHorizontalGlue());
        contentPanel.add(buttonPanel);

        btnLogin.addActionListener(this::onLogin);

        // Add content panel to animated container
        animatedContainer.add(contentPanel, BorderLayout.CENTER);

        // Add animated panel to outer container
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        outerPanel.add(animatedContainer, gbc);

        // Set up dialog
        setContentPane(outerPanel);
        setSize(500, 400);
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private void onLogin(ActionEvent ae) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        btnLogin.setEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    UserSession.getInstance().setUser(user);
                    dispose();
                    DashboardFrame.showFor(user);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginForm.this, e.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    public static void showDialog(Frame parent) {
        LoginForm dlg = new LoginForm(parent);
        dlg.setVisible(true);
    }
}
