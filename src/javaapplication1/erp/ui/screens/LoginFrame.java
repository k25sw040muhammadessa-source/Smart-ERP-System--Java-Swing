package javaapplication1.erp.ui.screens;

import javaapplication1.erp.dao.impl.UserDAOImpl;
import javaapplication1.erp.model.User;
import javaapplication1.erp.service.AuthManager;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.UserSession;
import javaapplication1.erp.util.WindowUtil;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;

public class LoginFrame extends Frame {

    private TextField usernameField;
    private TextField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private Checkbox rememberMe;
    private final Runnable onLoginSuccess;
    private String pendingUsername;
    private String pendingPassword;

    public LoginFrame(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setTitle("Smart ERP");
        // Reduced overall window size so system title bar controls remain visible on smaller displays
        setSize(940, 580);
        // Set window icon to app logo (replace Java coffee icon)
        try {
            setIconImage(javaapplication1.erp.ui.theme.Assets.getIconScaled("logo", 64));
        } catch (Throwable ignored) {}
        WindowUtil.centerWindow(this);
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());
        initUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void initUI() {
        Panel root = new Panel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        add(root, BorderLayout.CENTER);

        Panel leftVisual = new VisualPanel();
        leftVisual.setPreferredSize(new Dimension(320, 0));
        root.add(leftVisual, BorderLayout.WEST);

        Panel rightPanel = new Panel(new GridBagLayout());
        rightPanel.setBackground(Theme.BACKGROUND);
        root.add(rightPanel, BorderLayout.CENTER);

        Panel card = new Panel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(460, 520));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 24, 8, 24);

        Label title = new Label("Welcome Back");
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setForeground(Theme.PRIMARY_BLUE);
        c.gridy = 0;
        c.insets = new Insets(26, 32, 4, 32);
        card.add(title, c);

        Label subtitle = new Label("Sign in to manage Smart ERP");
        subtitle.setFont(Theme.FONT_SUBHEADING);
        subtitle.setForeground(Theme.TEXT_MUTED);
        c.gridy = 1;
        c.insets = new Insets(0, 32, 12, 32);
        card.add(subtitle, c);

        Label u = new Label("Email / Username");
        u.setFont(Theme.FONT_SMALL);
        u.setForeground(Theme.TEXT_MUTED);
        c.gridy = 2;
        c.insets = new Insets(12, 32, 4, 32);
        card.add(u, c);

        usernameField = new TextField();
        usernameField.setPreferredSize(new Dimension(380, Theme.INPUT_HEIGHT));
        usernameField.setFont(Theme.FONT_BODY);
        c.gridy = 3;
        c.insets = new Insets(0, 32, 10, 32);
        card.add(usernameField, c);

        Label p = new Label("Password");
        p.setFont(Theme.FONT_SMALL);
        p.setForeground(Theme.TEXT_MUTED);
        c.gridy = 4;
        c.insets = new Insets(0, 32, 4, 32);
        card.add(p, c);

        passwordField = new TextField();
        passwordField.setEchoChar('*');
        passwordField.setPreferredSize(new Dimension(380, Theme.INPUT_HEIGHT));
        passwordField.setFont(Theme.FONT_BODY);
        c.gridy = 5;
        c.insets = new Insets(0, 32, 8, 32);
        card.add(passwordField, c);

        errorLabel = new Label("");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.ERROR_RED);
        c.gridy = 6;
        c.insets = new Insets(0, 32, 8, 32);
        card.add(errorLabel, c);

        rememberMe = new Checkbox("Remember me");
        rememberMe.setForeground(Theme.TEXT_MUTED);
        rememberMe.setBackground(Color.WHITE);
        rememberMe.setFont(Theme.FONT_SMALL);
        c.gridy = 7;
        c.insets = new Insets(0, 32, 8, 32);
        card.add(rememberMe, c);

        Panel actions = new Panel(new BorderLayout());
        actions.setBackground(Color.WHITE);
        loginButton = new Button("LOGIN");
        loginButton.setPreferredSize(new Dimension(380, Theme.BUTTON_HEIGHT));
        loginButton.setBackground(Theme.PRIMARY_BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(Theme.FONT_BODY_MEDIUM);
        loginButton.addActionListener(e -> handleLogin());
        actions.add(loginButton, BorderLayout.CENTER);
        c.gridy = 8;
        c.insets = new Insets(0, 32, 8, 32);
        card.add(actions, c);

        LinkLabel forgot = new LinkLabel("Forgot Password?", this::openForgotPasswordDialog);
        c.gridy = 9;
        c.insets = new Insets(0, 32, 2, 32);
        card.add(forgot, c);

        LinkLabel signupLink = new LinkLabel("New here? Sign Up", this::openSignUpDialog);
        c.gridy = 10;
        c.insets = new Insets(0, 32, 10, 32);
        card.add(signupLink, c);

        DividerPanel divider = new DividerPanel("OR");
        c.gridy = 11;
        c.insets = new Insets(2, 32, 12, 32);
        card.add(divider, c);

        Button guest = new Button("Continue as Viewer");
        guest.addActionListener(e -> errorLabel.setText("Viewer mode coming soon."));
        guest.setBackground(new Color(0xF2F6FC));
        guest.setForeground(Theme.TEXT_MUTED);
        guest.setPreferredSize(new Dimension(380, Theme.BUTTON_HEIGHT));
        c.gridy = 12;
        c.insets = new Insets(0, 32, 24, 32);
        card.add(guest, c);

        rightPanel.add(card);

        Panel foot = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        foot.setBackground(Theme.BACKGROUND);
        Label about = new Label("About");
        Label bar1 = new Label("|");
        Label help = new Label("Help");
        Label bar2 = new Label("|");
        Label more = new Label("More");
        Label copy = new Label("© 2026 Smart ERP");
        about.setForeground(Theme.TEXT_MUTED);
        help.setForeground(Theme.TEXT_MUTED);
        more.setForeground(Theme.TEXT_MUTED);
        copy.setForeground(Theme.TEXT_MUTED);
        foot.add(about); foot.add(bar1); foot.add(help); foot.add(bar2); foot.add(more); foot.add(copy);
        add(foot, BorderLayout.SOUTH);

        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> handleLogin());
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        errorLabel.setText("");

        if (username.isEmpty()) {
            errorLabel.setText("Please enter username/email.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Please enter password.");
            passwordField.requestFocus();
            return;
        }
        AuthManager.LoginResult result = AuthManager.authenticate(username, password);
        if (!result.success) {
            errorLabel.setForeground(Theme.ERROR_RED);
            errorLabel.setText(result.message);
            return;
        }

        pendingUsername = username;
        pendingPassword = password;

        // ---- FIX: Store logged-in user in session ----
        try {
            java.util.Optional<User> maybeUser = new UserDAOImpl().findByUsername(username.trim().toLowerCase());
            maybeUser.ifPresent(u -> UserSession.getInstance().setUser(u));
        } catch (Exception ignored) {}

        if (result.forcePasswordChange) {
            errorLabel.setForeground(new Color(0x2E7D32));
            errorLabel.setText(result.message);
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, pendingUsername, pendingPassword, this::openDashboard);
            dialog.setVisible(true);
            return;
        }
        openDashboard();
    }

    private void openDashboard() {
        setVisible(false);
        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }

    private void openForgotPasswordDialog() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog(this, () -> {
            errorLabel.setForeground(new Color(0x2E7D32));
            errorLabel.setText("Reset request submitted. Wait for admin approval.");
        });
        dialog.setVisible(true);
    }

    private void openSignUpDialog() {
        SignUpDialog dialog = new SignUpDialog(this, () -> {
            errorLabel.setForeground(new Color(0x2E7D32));
            errorLabel.setText("Account created successfully.");
        });
        dialog.setVisible(true);
    }

    private static final class DividerPanel extends Canvas {
        private final String text;
        DividerPanel(String text) {
            this.text = text;
            setPreferredSize(new Dimension(360, 22));
        }
        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int y = getHeight() / 2;
            g2d.setColor(Theme.BORDER);
            g2d.drawLine(0, y, getWidth() / 2 - 18, y);
            g2d.drawLine(getWidth() / 2 + 18, y, getWidth(), y);
            g2d.setColor(Theme.TEXT_MUTED);
            g2d.setFont(Theme.FONT_SMALL);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y + 4);
        }
    }

    private static final class LinkLabel extends Canvas implements MouseListener {
        private final String text;
        private final Runnable callback;
        private boolean hover;
        LinkLabel(String text, Runnable callback) {
            this.text = text;
            this.callback = callback;
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(360, 20));
            addMouseListener(this);
        }
        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Theme.PRIMARY_BLUE);
            g2d.setFont(Theme.FONT_LINK);
            FontMetrics fm = g2d.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(text, 0, y);
            if (hover) {
                g2d.drawLine(0, y + 2, fm.stringWidth(text), y + 2);
            }
        }
        @Override public void mouseClicked(MouseEvent e) { if (callback != null) callback.run(); }
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
        @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
    }

    private static final class VisualPanel extends Panel {
        private float phase;
        private final java.util.Timer timer = new java.util.Timer(true);
        private Image buffer;
        private boolean needsRepaint;

        VisualPanel() {
            timer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    phase += 0.05f;
                    needsRepaint = true;
                    repaint();
                }
            }, 0, 50); // Reduced frequency from 40ms to 50ms to reduce flickering
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }

        @Override
        public void paint(Graphics g) {
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            
            // Create or resize buffer if needed
            if (buffer == null || buffer.getWidth(this) != w || buffer.getHeight(this) != h) {
                buffer = createImage(w, h);
            }
            
            if (buffer == null) {
                return; // Prevent NPE if createImage fails
            }
            
            Graphics2D g2d = (Graphics2D) buffer.getGraphics();
            if (g2d == null) {
                return; // Prevent NPE if getGraphics fails
            }
            
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setPaint(new GradientPaint(0, 0, new Color(0x0D47A1), w, h, new Color(0x0A7CFF)));
                g2d.fillRect(0, 0, w, h);

                for (int i = 0; i < 3; i++) {
                    float speed = 0.6f + (i * 0.28f);
                    int radius = 160 + (i * 40);
                    int x = (int) (Math.sin(phase * speed + i) * 42) + (w / 3) - (i * 22);
                    int y = (int) (Math.cos(phase * (speed + 0.2f) + i) * 38) + (h / 4) + (i * 120);
                    g2d.setColor(new Color(255, 255, 255, 24 - i * 5));
                    g2d.fill(new Ellipse2D.Float(x - radius / 2f, y - radius / 2f, radius, radius));
                }

                for (int i = 0; i < 16; i++) {
                    float f = (phase * 0.35f) + i * 0.6f;
                    int x = (int) ((Math.sin(f) * 0.4 + 0.5) * w);
                    int y = (int) ((Math.cos(f * 1.2f) * 0.35 + 0.5) * h);
                    int alpha = 65 + (int) ((Math.sin(f * 1.8f) + 1) * 30);
                    g2d.setColor(new Color(255, 255, 255, Math.min(120, alpha)));
                    g2d.fillOval(x, y, 4, 4);
                }

                int logoSize = Math.min(240, Math.max(80, (int) (w * 0.6)));
                int logoX = (w - logoSize) / 2;
                int logoY = 28;
                Image logo = javaapplication1.erp.ui.theme.Assets.getIconScaled("logo", logoSize);
                if (logo != null) {
                    g2d.drawImage(logo, logoX, logoY, this);
                }

                // Tagline (centered below logo)
                String tagline = "Smarter control for your business";
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
                g2d.setColor(new Color(229, 242, 255));
                FontMetrics fmTag = g2d.getFontMetrics();
                int tagX = (w - fmTag.stringWidth(tagline)) / 2;
                int tagY = logoY + logoSize + 26;
                g2d.drawString(tagline, tagX, tagY);
                
                g.drawImage(buffer, 0, 0, this);
            } finally {
                g2d.dispose();
            }
        }
    }
}