package javaapplication1.erp.ui;

import javaapplication1.erp.model.User;
import javaapplication1.erp.ui.dashboard.DashboardPanel;
import javaapplication1.erp.ui.dashboard.DemoDashboardDataProvider;
import javaapplication1.erp.ui.sidebar.SidebarGroup;
import javaapplication1.erp.util.Permission;
import javaapplication1.erp.util.UserSession;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.Assets;
import javaapplication1.erp.ui.theme.SidebarButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * DashboardFrame: modern main window shell with grouped sidebar and dashboard widgets.
 */
public class DashboardFrame extends JFrame {

    private static final String PAGE_DASHBOARD = "dashboard";

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final Map<String, JPanel> contentCache = new HashMap<>();

    private final JLabel pageTitle = new JLabel("Dashboard");
    private final JButton notificationButton = new JButton("Notifications (0)");

    private final Map<String, SidebarButton> navButtons = new HashMap<>();
    private SidebarButton selectedNav;
    private boolean sidebarCompact = false;

    private DashboardPanel dashboardPanel;

    public DashboardFrame(User user) {
        super("Smart ERP - Dashboard");
        initUI(user);
        // Set window icon to app logo (replace Java coffee icon)
        try {
            setIconImage(Assets.getIconScaled("logo", 64));
        } catch (Throwable ignored) {}
    }

    private void initUI(User user) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        sidebar.setBackground(Theme.BACKGROUND);
        sidebar.setAlignmentY(Component.TOP_ALIGNMENT);
        // Allow sidebar to grow vertically for scrolling
        sidebar.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, Integer.MAX_VALUE));
        final JScrollPane[] sidebarScrollRef = new JScrollPane[1];

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);

        JPanel logoLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoLeft.setOpaque(false);
        java.awt.Image logo = Assets.getIconScaled("logo", 64);
        if (logo != null) {
            logoLeft.add(new JLabel(new ImageIcon(logo)));
        }
        // Keep a placeholder label for compatibility with compact toggle, but hide text
        JLabel appTitle = new JLabel("");
        appTitle.setFont(Theme.FONT_SUBHEADING);
        appTitle.setForeground(Theme.TEXT_PRIMARY);
        appTitle.setVisible(false);
        logoLeft.add(appTitle);

        JButton toggleSidebar = new JButton("Collapse");
        toggleSidebar.setFocusPainted(false);
        toggleSidebar.setFont(Theme.FONT_SMALL);
        toggleSidebar.setPreferredSize(new Dimension(70, 24));
        toggleSidebar.addActionListener(e -> {
            sidebarCompact = !sidebarCompact;
            if (sidebarScrollRef[0] != null) {
                setSidebarCompact(sidebar, sidebarScrollRef[0], appTitle, toggleSidebar);
            }
        });

        logoPanel.add(logoLeft, BorderLayout.WEST);
        logoPanel.add(toggleSidebar, BorderLayout.EAST);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(6));

        SidebarButton btnDashboard = createNavButton("Dashboard", "dashboard", PAGE_DASHBOARD, "Dashboard");
        btnDashboard.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(btnDashboard);
        sidebar.add(Box.createVerticalStrut(4));

        SidebarGroup masterGroup = new SidebarGroup("Master");
        SidebarGroup operationsGroup = new SidebarGroup("Operations");
        SidebarGroup financeGroup = new SidebarGroup("Finance");
        SidebarGroup reportsGroup = new SidebarGroup("Reports");
        SidebarGroup systemGroup = new SidebarGroup("System");

        if (UserSession.getInstance().hasPermission(Permission.MANAGE_PRODUCTS)) {
            masterGroup.addItem(createNavButton("Products", "product", "products", "Products"));
        }
        if (UserSession.getInstance().hasPermission(Permission.MANAGE_CUSTOMERS)) {
            masterGroup.addItem(createNavButton("Customers", "customer", "customers", "Customers"));
        }
        if (UserSession.getInstance().hasPermission(Permission.MANAGE_SUPPLIERS)) {
            masterGroup.addItem(createNavButton("Suppliers", "supplier", "suppliers", "Suppliers"));
        }
        if (UserSession.getInstance().hasPermission(Permission.MANAGE_EMPLOYEES)) {
            masterGroup.addItem(createNavButton("Employees", "employee", "employees", "Employees"));
        }

        if (UserSession.getInstance().hasPermission(Permission.MANAGE_INVENTORY)) {
            operationsGroup.addItem(createNavButton("Inventory", "inventory", "inventory", "Inventory"));
        }
        if (UserSession.getInstance().hasPermission(Permission.CREATE_SALES)) {
            operationsGroup.addItem(createNavButton("Sales", "sales", "sales", "Sales"));
        }
        if (UserSession.getInstance().hasPermission(Permission.CREATE_PURCHASES)) {
            operationsGroup.addItem(createNavButton("Purchases", "purchase", "purchases", "Purchases"));
        }

        if (UserSession.getInstance().hasPermission(Permission.VIEW_REPORTS)) {
            financeGroup.addItem(createNavButton("Payments", "payment", "payments", "Payments"));
            financeGroup.addItem(createNavButton("Ledgers", "ledger", "ledgers", "Ledgers"));
        }

        if (UserSession.getInstance().hasPermission(Permission.MANAGE_INVENTORY)) {
            reportsGroup.addItem(createNavButton("Stock Report", "stock", "lowStock", "Stock Report"));
        }
        if (UserSession.getInstance().hasPermission(Permission.CREATE_SALES)) {
            reportsGroup.addItem(createNavButton("Sales Report", "sales", "sales", "Sales Report"));
        }

        if (UserSession.getInstance().hasPermission(Permission.MANAGE_USERS)) {
            systemGroup.addItem(createNavButton("Users", "user", "users", "Users"));
            systemGroup.addItem(createNavButton("Permissions", "permission", "permissions", "Permissions"));
        }
        if (UserSession.getInstance().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            systemGroup.addItem(createNavButton("Audit Logs", "audit", "auditLogs", "Audit Logs"));
        }

        addGroupIfNotEmpty(sidebar, masterGroup);
        addGroupIfNotEmpty(sidebar, operationsGroup);
        addGroupIfNotEmpty(sidebar, financeGroup);
        addGroupIfNotEmpty(sidebar, reportsGroup);
        addGroupIfNotEmpty(sidebar, systemGroup);

        JScrollPane sidebarScroll = new JScrollPane(sidebar,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScrollRef[0] = sidebarScroll;
        sidebarScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebarScroll.getViewport().setBackground(Theme.BACKGROUND);
        sidebarScroll.setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(18);
        sidebarScroll.setWheelScrollingEnabled(true);

        add(sidebarScroll, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.TOPBAR_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        pageTitle.setFont(Theme.FONT_HEADING);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel userLabel = new JLabel("Welcome, " + user.getUsername());
        userLabel.setFont(Theme.FONT_SMALL);
        userLabel.setForeground(Theme.TEXT_MUTED);

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.add(pageTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(userLabel);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);

        JButton quickAddButton = createQuickAddButton();
        styleHeaderButton(notificationButton);

        headerRight.add(quickAddButton);
        headerRight.add(notificationButton);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        right.add(header, BorderLayout.NORTH);

        contentPanel.setOpaque(true);
        contentPanel.setBackground(Theme.BACKGROUND);

        dashboardPanel = new DashboardPanel(new DemoDashboardDataProvider(), this::openModule);
        dashboardPanel.setNotificationListener(count -> notificationButton.setText("Notifications (" + count + ")"));

        contentPanel.add(dashboardPanel, PAGE_DASHBOARD);
        contentCache.put(PAGE_DASHBOARD, dashboardPanel);

        // Wrap contentPanel in a scroll-friendly container with BoxLayout
        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
        contentWrapper.setBackground(Theme.BACKGROUND);
        contentWrapper.add(contentPanel);
        contentWrapper.add(Box.createVerticalGlue());
        
        JScrollPane contentScroll = new JScrollPane(contentWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getViewport().setBackground(Theme.BACKGROUND);
        
        right.add(contentScroll, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        selectNav(btnDashboard);
        contentLayout.show(contentPanel, PAGE_DASHBOARD);
        dashboardPanel.refreshAsync("All", "", null);

        setSize(1360, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void styleHeaderButton(AbstractButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(Theme.FONT_BODY);
    }

    private JButton createQuickAddButton() {
        JButton btn = new JButton("Quick Add");
        styleHeaderButton(btn);

        JPopupMenu menu = new JPopupMenu();
        if (UserSession.getInstance().hasPermission(Permission.CREATE_SALES)) {
            menu.add(createQuickMenuItem("New Sale", () -> openModule("sales")));
        }
        if (UserSession.getInstance().hasPermission(Permission.MANAGE_PRODUCTS)) {
            menu.add(createQuickMenuItem("Add Product", () -> openModule("products")));
        }
        if (UserSession.getInstance().hasPermission(Permission.MANAGE_CUSTOMERS)) {
            menu.add(createQuickMenuItem("Add Customer", () -> openModule("customers")));
        }

        if (menu.getComponentCount() == 0) {
            JMenuItem empty = new JMenuItem("No quick actions available");
            empty.setEnabled(false);
            menu.add(empty);
        }

        btn.addActionListener(e -> menu.show(btn, 0, btn.getHeight()));
        return btn;
    }

    private JMenuItem createQuickMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }



    private void setSidebarCompact(JPanel sidebar, JScrollPane sidebarScroll, JLabel appTitle, JButton toggleButton) {
        int width = sidebarCompact ? 92 : Theme.SIDEBAR_WIDTH;
        int preferredHeight = Math.max(sidebar.getPreferredSize().height, sidebar.getMinimumSize().height);
        if (preferredHeight <= 0) {
            preferredHeight = sidebar.getLayout().preferredLayoutSize(sidebar).height;
        }
        sidebar.setPreferredSize(new Dimension(width, preferredHeight));
        sidebar.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        sidebarScroll.setPreferredSize(new Dimension(width, 0));
        appTitle.setVisible(!sidebarCompact);
        toggleButton.setText(sidebarCompact ? "Expand" : "Collapse");

        for (SidebarButton btn : navButtons.values()) {
            btn.setCompact(sidebarCompact);
        }

        sidebar.revalidate();
        sidebar.repaint();
    }

    private SidebarButton createNavButton(String label, String iconName, String pageKey, String title) {
        SidebarButton btn = new SidebarButton(label, iconName);
        btn.addActionListener(e -> {
            selectNav(btn);
            openPage(pageKey, title, panelSupplier(pageKey));
        });
        navButtons.put(pageKey, btn);
        return btn;
    }

    private void addGroupIfNotEmpty(JPanel sidebar, SidebarGroup group) {
        if (group.isEmpty()) {
            return;
        }
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(group);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private void openModule(String moduleKey) {
        SidebarButton button = navButtons.get(moduleKey);
        if (button == null && !PAGE_DASHBOARD.equals(moduleKey)) {
            return;
        }

        if (button != null) {
            selectNav(button);
        }
        openPage(moduleKey, prettyTitle(moduleKey), panelSupplier(moduleKey));
    }

    private void openPage(String pageKey, String title, Supplier<JPanel> panelSupplier) {
        JPanel panel = contentCache.get(pageKey);
        if (panel == null) {
            panel = panelSupplier.get();
            contentCache.put(pageKey, panel);
            contentPanel.add(panel, pageKey);
        }

        contentLayout.show(contentPanel, pageKey);
        pageTitle.setText(title);
    }

    private Supplier<JPanel> panelSupplier(String key) {
        switch (key) {
            case PAGE_DASHBOARD:
                return () -> dashboardPanel;
            case "products":
                return ProductListPanel::new;
            case "customers":
                return CustomerListPanel::new;
            case "suppliers":
                return SupplierListPanel::new;
            case "employees":
                return EmployeeListPanel::new;
            case "inventory":
                return InventoryPanel::new;
            case "sales":
                return SalesPanel::new;
            case "purchases":
                return PurchasePanel::new;
            case "users":
                return UserManagementPanel::new;
            case "permissions":
                return PermissionManagementPanel::new;
            case "lowStock":
                return LowStockPanel::new;
            case "payments":
                return PaymentPanel::new;
            case "ledgers":
                return LedgerPanel::new;
            case "auditLogs":
                return AuditLogPanel::new;
            default:
                return JPanel::new;
        }
    }

    private String prettyTitle(String key) {
        switch (key) {
            case PAGE_DASHBOARD:
                return "Dashboard";
            case "products":
                return "Products";
            case "customers":
                return "Customers";
            case "suppliers":
                return "Suppliers";
            case "employees":
                return "Employees";
            case "inventory":
                return "Inventory";
            case "sales":
                return "Sales";
            case "purchases":
                return "Purchases";
            case "payments":
                return "Payments";
            case "ledgers":
                return "Ledgers";
            case "lowStock":
                return "Stock Report";
            case "permissions":
                return "Permissions";
            case "users":
                return "Users";
            case "auditLogs":
                return "Audit Logs";
            default:
                return "Dashboard";
        }
    }

    private String getCurrentCard() {
        for (Map.Entry<String, JPanel> entry : contentCache.entrySet()) {
            if (entry.getValue().isShowing()) {
                return entry.getKey();
            }
        }
        return PAGE_DASHBOARD;
    }

    private void selectNav(SidebarButton btn) {
        if (selectedNav != null) selectedNav.setSelectedState(false);
        selectedNav = btn;
        if (selectedNav != null) selectedNav.setSelectedState(true);
    }

    public static void showFor(User u) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame f = new DashboardFrame(u);
            f.setVisible(true);
        });
    }
}
