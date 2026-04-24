package javaapplication1.erp.ui.screens;

import javaapplication1.erp.ui.screens.panels.ContentPanel;
import javaapplication1.erp.ui.screens.panels.SidebarPanel;
import javaapplication1.erp.ui.screens.panels.TopBarPanel;
import javaapplication1.erp.ui.screens.AboutDialog;
import javaapplication1.erp.ui.screens.HelpDialog;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.WindowUtil;
import javaapplication1.erp.util.UserSession;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Dashboard frame with sidebar navigation and content area.
 */
public class DashboardFrame extends Frame {

    private SidebarPanel sidebarPanel;
    private TopBarPanel topBarPanel;
    private ContentPanel contentPanel;
    private String activePanelName = "Dashboard";
    private final Deque<String> navigationHistory = new ArrayDeque<>();
    private boolean backNavigationInProgress = false;
    private final Runnable onLogout;

    public DashboardFrame() {
        this(null);
    }

    public DashboardFrame(Runnable onLogout) {
        this.onLogout = onLogout;
        setTitle("Smart ERP - Dashboard");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 680));
        WindowUtil.centerWindow(this);
        // Set window icon to app logo (replace Java coffee icon)
        try {
            setIconImage(javaapplication1.erp.ui.theme.Assets.getIconScaled("logo", 64));
        } catch (Throwable ignored) {}
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void initUI() {
        // Initialize contentPanel first, before SidebarPanel
        contentPanel = new ContentPanel();
        
        sidebarPanel = new SidebarPanel(() -> {
            String selected = sidebarPanel.getSelectedNav();
            if (!backNavigationInProgress && activePanelName != null && !activePanelName.equals(selected)) {
                navigationHistory.push(activePanelName);
            }
            activePanelName = selected;
            contentPanel.showPanel(activePanelName);
            topBarPanel.setBackEnabled(!navigationHistory.isEmpty());
        });
        add(sidebarPanel, BorderLayout.WEST);

        Panel mainPanel = new Panel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);
        mainPanel.setForeground(Theme.TEXT_PRIMARY);

        topBarPanel = new TopBarPanel();
        topBarPanel.setOnBack(this::goBack);
        topBarPanel.setOnAbout(() -> new AboutDialog(this).setVisible(true));
        topBarPanel.setOnHelp(() -> new HelpDialog(this).setVisible(true));
        topBarPanel.setOnLogout(this::logout);
        topBarPanel.setOnExit(() -> System.exit(0));

        // Show logged-in user info in title
        javaapplication1.erp.model.User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            setTitle("Smart ERP  |  " + currentUser.getUsername());
        }

        mainPanel.add(topBarPanel, BorderLayout.NORTH);  // FIX: was SOUTH

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Ensure initial content panel matches the sidebar selection after construction
        activePanelName = sidebarPanel.getSelectedNav();
        contentPanel.showPanel(activePanelName);
        topBarPanel.setBackEnabled(false);
    }

    private void goBack() {
        if (navigationHistory.isEmpty()) {
            topBarPanel.setBackEnabled(false);
            return;
        }

        String previous = navigationHistory.pop();
        backNavigationInProgress = true;
        try {
            if (!sidebarPanel.selectNav(previous)) {
                activePanelName = previous;
                contentPanel.showPanel(previous);
            }
        } finally {
            backNavigationInProgress = false;
            topBarPanel.setBackEnabled(!navigationHistory.isEmpty());
        }
    }

    private void logout() {
        UserSession.getInstance().logout();
        if (onLogout != null) {
            onLogout.run();
            return;
        }
        dispose();
    }
}