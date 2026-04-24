package javaapplication1.erp.ui.screens.panels;

import javaapplication1.erp.ui.components.NavItem;
import javaapplication1.erp.ui.theme.Assets;
import javaapplication1.erp.ui.theme.Theme;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Sidebar panel with navigation items.
 */
public class SidebarPanel extends Panel {

    private List<NavItem> navItems = new ArrayList<>();
    private NavItem selectedItem;
    private Runnable onNavChange;
    private Panel navList;
    private Panel navContainer;
    private ScrollPane navScrollPane;

    public SidebarPanel(Runnable onNavChange) {
        this.onNavChange = onNavChange;

        setBackground(Theme.SURFACE);
        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
        setLayout(new BorderLayout());

        navList = new Panel(new GridLayout(0, 1, 0, 0));
        navList.setBackground(Theme.SURFACE);
        navContainer = new Panel(new BorderLayout());
        navContainer.setBackground(Theme.SURFACE);
        navContainer.add(navList, BorderLayout.NORTH);

        navScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        navScrollPane.setBackground(Theme.SURFACE);
        navScrollPane.add(navContainer);
        navScrollPane.getVAdjustable().setUnitIncrement(24);
        navScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncSidebarWidthToViewport();
            }
        });
        addMouseWheelScroll(navScrollPane, navList, navContainer);

        add(navScrollPane, BorderLayout.CENTER);

        initNavItems(navList);
        EventQueue.invokeLater(this::syncSidebarWidthToViewport);
    }

    private void syncSidebarWidthToViewport() {
        int viewportWidth = Math.max(0, navScrollPane.getViewportSize().width);
        int contentHeight = navContainer.getPreferredSize().height;
        int listHeight = navList.getPreferredSize().height;

        navList.setPreferredSize(new Dimension(viewportWidth, listHeight));
        navContainer.setPreferredSize(new Dimension(viewportWidth, contentHeight));
        navList.revalidate();
        navContainer.revalidate();
    }

    private void addMouseWheelScroll(ScrollPane scrollPane, Component... wheelTargets) {
        MouseWheelListener wheelListener = event -> {
            Adjustable adjustable = scrollPane.getVAdjustable();
            int increment = Math.max(16, adjustable.getUnitIncrement());
            int delta = event.getWheelRotation() * increment;

            int min = adjustable.getMinimum();
            int max = adjustable.getMaximum() - adjustable.getVisibleAmount();
            int next = Math.max(min, Math.min(max, adjustable.getValue() + delta));
            adjustable.setValue(next);
            event.consume();
        };

        scrollPane.addMouseWheelListener(wheelListener);
        for (Component target : wheelTargets) {
            target.addMouseWheelListener(wheelListener);
        }
    }

    private void initNavItems(Panel navList) {
        javaapplication1.erp.util.UserSession session = javaapplication1.erp.util.UserSession.getInstance();

        addNavItem("Dashboard", "dashboard");

        if (session.hasPermission(javaapplication1.erp.util.Permission.MANAGE_PRODUCTS)) {
            addNavItem("Products", "products");
            addNavItem("Units", "units");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.MANAGE_CUSTOMERS)) {
            addNavItem("Customers", "customers");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.MANAGE_SUPPLIERS)) {
            addNavItem("Suppliers", "suppliers");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.MANAGE_EMPLOYEES)) {
            addNavItem("Employees", "employees");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.MANAGE_INVENTORY)) {
            addNavItem("Inventory", "inventory");
            addNavItem("Stock", "stock");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.CREATE_SALES)) {
            addNavItem("Sales", "sales");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.CREATE_PURCHASES)) {
            addNavItem("Purchases", "purchases");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.VIEW_REPORTS)) {
            addNavItem("Payments", "payments");
            addNavItem("Ledgers", "ledgers");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.MANAGE_USERS)) {
            addNavItem("Users", "users");
            addNavItem("Permissions", "permissions");
            addNavItem("Pending Requests", "permissions");
        }
        if (session.hasPermission(javaapplication1.erp.util.Permission.VIEW_AUDIT_LOGS)) {
            addNavItem("Audit Logs", "auditlogs");
            addNavItem("Audit Logs (Legacy)", "auditlogs");
        }

        for (NavItem item : navItems) {
            navList.add(item);
        }
        syncSidebarWidthToViewport();

        // Select first item without triggering external callback during construction
        if (!navItems.isEmpty()) {
            selectedItem = navItems.get(0);
            selectedItem.setSelected(true);
        }
    }

    private void addNavItem(String label, String iconName) {
        NavItem item = new NavItem(label, Assets.getIcon(iconName), null);
        item.setOnClick(() -> selectItem(item));
        navItems.add(item);
    }

    private void selectItem(NavItem item) {
        if (selectedItem != null) {
            selectedItem.setSelected(false);
        }
        selectedItem = item;
        item.setSelected(true);

        if (onNavChange != null) {
            onNavChange.run();
        }
    }

    public String getSelectedNav() {
        return selectedItem != null ? selectedItem.getLabel() : "Dashboard";
    }

    public boolean selectNav(String label) {
        if (label == null || label.isBlank()) {
            return false;
        }
        for (NavItem item : navItems) {
            if (label.equals(item.getLabel())) {
                selectItem(item);
                return true;
            }
        }
        return false;
    }
}