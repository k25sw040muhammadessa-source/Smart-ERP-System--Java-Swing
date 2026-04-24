package javaapplication1.erp.ui.screens.panels;

import javaapplication1.erp.model.User;
import javaapplication1.erp.ui.components.RoundedButton;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.UserSession;

import java.awt.*;

/**
 * Top bar panel with logged-in user info and action buttons.
 */
public class TopBarPanel extends Panel {

    private RoundedButton backButton;
    private RoundedButton aboutButton;
    private RoundedButton helpButton;
    private RoundedButton logoutButton;
    private RoundedButton exitButton;
    private Runnable onBack;
    private Runnable onAbout;
    private Runnable onHelp;
    private Runnable onLogout;
    private Runnable onExit;
    private final Label userInfoLabel = new Label("", Label.LEFT);

    public TopBarPanel() {
        setBackground(Theme.TOPBAR_BACKGROUND);
        setPreferredSize(new Dimension(0, 64));
        setLayout(new BorderLayout());

        // ---- Left: User info and Logo ----
        Panel leftPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 16, 11));
        leftPanel.setBackground(Theme.TOPBAR_BACKGROUND);

        javax.swing.JComponent brandLogo = new javax.swing.JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int imgSize = 44;
                Image logo = javaapplication1.erp.ui.theme.Assets.getIconScaled("logo", imgSize);
                if (logo != null) {
                    int y = Math.max(0, (getHeight() - imgSize) / 2);
                    g2d.drawImage(logo, 0, y, this);
                }
                // separator after logo
                g2d.setColor(Theme.BORDER);
                int sepX = imgSize + 16;
                int sepY = 6;
                int sepH = Math.max(22, getHeight() - 12);
                g2d.fillRect(sepX, sepY, 2, sepH);
            }
        };
        brandLogo.setPreferredSize(new Dimension(140, 56));
        leftPanel.add(brandLogo);

        refreshUserInfo();
        userInfoLabel.setFont(Theme.FONT_BODY_MEDIUM);
        userInfoLabel.setForeground(Theme.TEXT_PRIMARY);
        leftPanel.add(userInfoLabel);

        // ---- Right: Action buttons ----
        Panel actionPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 8, 11));
        actionPanel.setBackground(Theme.TOPBAR_BACKGROUND);

        Color btnBg = Theme.PRIMARY_BLUE;
        Color btnHover = Theme.PRIMARY_HOVER;
        Color btnPressed = Theme.DARK_BLUE_ACCENT;
        Color textColor = Color.WHITE;

        backButton = new RoundedButton("Back", () -> { if (onBack != null) onBack.run(); });
        backButton.setColors(new Color(0xF1F5F9), new Color(0xE2E8F0), new Color(0xCBD5E1));
        backButton.setTextColor(Theme.TEXT_MUTED);
        backButton.setEnabled(false);
        backButton.setPreferredSize(new Dimension(110, Theme.BUTTON_HEIGHT));

        aboutButton = new RoundedButton("About", () -> { if (onAbout != null) onAbout.run(); });
        aboutButton.setColors(btnBg, btnHover, btnPressed);
        aboutButton.setTextColor(textColor);
        aboutButton.setFocusable(false);
        aboutButton.setPreferredSize(new Dimension(100, Theme.BUTTON_HEIGHT));

        helpButton = new RoundedButton("Help", () -> { if (onHelp != null) onHelp.run(); });
        helpButton.setColors(btnBg, btnHover, btnPressed);
        helpButton.setTextColor(textColor);
        helpButton.setPreferredSize(new Dimension(92, Theme.BUTTON_HEIGHT));

        logoutButton = new RoundedButton("Logout", () -> { if (onLogout != null) onLogout.run(); });
        logoutButton.setColors(btnBg, btnHover, btnPressed);
        logoutButton.setTextColor(textColor);
        logoutButton.setPreferredSize(new Dimension(110, Theme.BUTTON_HEIGHT));

        exitButton = new RoundedButton("Exit", () -> { if (onExit != null) onExit.run(); });
        exitButton.setColors(new Color(0xFEF2F2), new Color(0xFEE2E2), new Color(0xFECACA));
        exitButton.setTextColor(Theme.ERROR_RED);
        exitButton.setPreferredSize(new Dimension(96, Theme.BUTTON_HEIGHT));

        actionPanel.add(backButton);
        actionPanel.add(aboutButton);
        actionPanel.add(helpButton);
        actionPanel.add(logoutButton);
        actionPanel.add(exitButton);

        add(leftPanel, BorderLayout.WEST);
        add(actionPanel, BorderLayout.EAST);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Theme.TOPBAR_BACKGROUND);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Theme.PRIMARY_BLUE);
        g2d.fillRect(0, 0, getWidth(), 3);
        g2d.setColor(Theme.BORDER);
        g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        super.paint(g);
    }

    public String getSearchText() { return ""; }
    public void setSearchText(String text) {}

    public void setOnBack(Runnable r) { this.onBack = r; }
    public void setOnAbout(Runnable r) { this.onAbout = r; }
    public void setOnHelp(Runnable r) { this.onHelp = r; }
    public void setOnLogout(Runnable r) { this.onLogout = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }

    public void setBackEnabled(boolean enabled) {
        backButton.setEnabled(enabled);
        backButton.repaint();
    }

    public void refreshUserInfo() {
        User u = UserSession.getInstance().getUser();
        if (u != null) {
            String roleName = u.getRoleId() == 1 ? "Admin" : "User";
            userInfoLabel.setText("  Logged in as:  " + u.getUsername() + "   [" + roleName + "]");
        } else {
            userInfoLabel.setText("");
        }
    }
}
