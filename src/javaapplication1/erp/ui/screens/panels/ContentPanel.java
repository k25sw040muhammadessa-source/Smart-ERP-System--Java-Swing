package javaapplication1.erp.ui.screens.panels;

import javaapplication1.erp.ui.AuditLogPanel;
import javaapplication1.erp.ui.CustomerListPanel;
import javaapplication1.erp.ui.EmployeeListPanel;
import javaapplication1.erp.ui.InventoryPanel;
import javaapplication1.erp.ui.LedgerPanel;
import javaapplication1.erp.ui.LowStockPanel;
import javaapplication1.erp.ui.PaymentPanel;
import javaapplication1.erp.ui.PermissionManagementPanel;
import javaapplication1.erp.ui.ProductListPanel;
import javaapplication1.erp.ui.PurchasePanel;
import javaapplication1.erp.ui.SalesPanel;
import javaapplication1.erp.ui.SupplierListPanel;
import javaapplication1.erp.ui.UserManagementPanel;
import javaapplication1.erp.ui.UnitListPanel;
import javaapplication1.erp.ui.dashboard.DashboardDataProvider;
import javaapplication1.erp.ui.dashboard.DashboardPanel;
import javaapplication1.erp.ui.dashboard.DemoDashboardDataProvider;
import javaapplication1.erp.ui.theme.Theme;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;

/**
 * Content panel with CardLayout for switching between different views.
 */
public class ContentPanel extends Panel {

    private CardLayout cardLayout;
    private PendingRequestsPanel pendingRequestsPanel;
    private ProductListPanel productListPanel;
    private CustomerListPanel customerListPanel;
    private SupplierListPanel supplierListPanel;
    private EmployeeListPanel employeeListPanel;
    private InventoryPanel inventoryPanel;
    private SalesPanel salesPanel;
    private PurchasePanel purchasePanel;
    private PaymentPanel paymentPanel;
    private LedgerPanel ledgerPanel;
    private LowStockPanel lowStockPanel;
    private UnitListPanel unitListPanel;
    private PermissionManagementPanel permissionManagementPanel;
    private UserManagementPanel userManagementPanel;
    private AuditLogPanel auditLogPanel;
    private AuditLogsPanel legacyAuditLogsPanel;
    private DashboardPanel dashboardPanel;
    private final Set<String> cardNames = new HashSet<>();

    public ContentPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(Theme.SURFACE);

        // Add panels
        pendingRequestsPanel = new PendingRequestsPanel();
        productListPanel = new ProductListPanel();
        customerListPanel = new CustomerListPanel();
        supplierListPanel = new SupplierListPanel();
        employeeListPanel = new EmployeeListPanel();
        inventoryPanel = new InventoryPanel();
        salesPanel = new SalesPanel();
        purchasePanel = new PurchasePanel();
        paymentPanel = new PaymentPanel();
        ledgerPanel = new LedgerPanel();
        lowStockPanel = new LowStockPanel();
        unitListPanel = new UnitListPanel();
        permissionManagementPanel = new PermissionManagementPanel();
        userManagementPanel = new UserManagementPanel();
        auditLogPanel = new AuditLogPanel();
        legacyAuditLogsPanel = new AuditLogsPanel();

        addCard("Pending Requests", pendingRequestsPanel);
        addCard("Dashboard", createDashboardPanel());
        addCard("Products", wrapSwingPanel(productListPanel));
        addCard("Customers", wrapSwingPanel(customerListPanel));
        addCard("Suppliers", wrapSwingPanel(supplierListPanel));
        addCard("Employees", wrapSwingPanel(employeeListPanel));
        addCard("Inventory", wrapSwingPanel(inventoryPanel));
        addCard("Sales", wrapSwingPanel(salesPanel));
        addCard("Purchases", wrapSwingPanel(purchasePanel));
        addCard("Payments", wrapSwingPanel(paymentPanel));
        addCard("Ledgers", wrapSwingPanel(ledgerPanel));
        addCard("Stock", wrapSwingPanel(lowStockPanel));
        addCard("Units", wrapSwingPanel(unitListPanel));
        addCard("Permissions", wrapSwingPanel(permissionManagementPanel));
        addCard("Users", wrapSwingPanel(userManagementPanel));
        addCard("Audit Logs", wrapSwingPanel(auditLogPanel));
        addCard("Audit Logs (Legacy)", legacyAuditLogsPanel);

        // Keep fallback local panel for unknown names.
        addCard("__fallback__", createInfoPanel("Module", "Module not available."));
    }

    public void showPanel(String name) {
        if ("Pending Requests".equals(name)) {
            pendingRequestsPanel.refreshData();
        } else if ("Dashboard".equals(name) && dashboardPanel != null) {
            dashboardPanel.refreshAsync("", "", null);
        }
        cardLayout.show(this, cardNames.contains(name) ? name : "__fallback__");
    }

    private void addCard(String name, Component component) {
        add(component, name);
        cardNames.add(name);
    }

    private Panel wrapSwingPanel(Component component) {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private Panel createDashboardPanel() {
        DashboardDataProvider dataProvider = new DemoDashboardDataProvider();
        dashboardPanel = new DashboardPanel(dataProvider, moduleKey -> {
            String target = switch (moduleKey) {
                case "lowStock" -> "Stock";
                case "payments" -> "Payments";
                case "products" -> "Products";
                case "sales" -> "Sales";
                default -> "Dashboard";
            };
            showPanel(target);
        });
        dashboardPanel.refreshAsync("", "", null);
        return wrapSwingPanel(dashboardPanel);
    }

    private Panel createInfoPanel(String title, String message) {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        Label label = new Label(title + " - " + message);
        label.setFont(Theme.FONT_HEADING);
        label.setForeground(Theme.TEXT_MUTED);
        label.setAlignment(Label.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public void applyTopBarSearch(String panelName, String query) {
        String term = query == null ? "" : query.trim();
        switch (panelName) {
            case "Products" -> productListPanel.applySearch(term);
            case "Customers" -> customerListPanel.applySearch(term);
            case "Suppliers" -> supplierListPanel.applySearch(term);
            case "Employees" -> employeeListPanel.applySearch(term);
            case "Users" -> userManagementPanel.applySearch(term);
            case "Units" -> unitListPanel.applySearch(term);
            case "Audit Logs" -> auditLogPanel.applySearch(term);
            default -> {
                // Not all panels support search terms.
            }
        }
    }

    public void refreshPanel(String panelName) {
        switch (panelName) {
            case "Products" -> productListPanel.refreshData();
            case "Customers" -> customerListPanel.refreshData();
            case "Suppliers" -> supplierListPanel.refreshData();
            case "Employees" -> employeeListPanel.refreshData();
            case "Inventory" -> inventoryPanel.refreshData();
            case "Sales" -> salesPanel.refreshData();
            case "Purchases" -> purchasePanel.refreshData();
            case "Payments" -> paymentPanel.refreshData();
            case "Ledgers" -> ledgerPanel.refreshData();
            case "Stock" -> lowStockPanel.refreshData();
            case "Units" -> unitListPanel.refreshData();
            case "Permissions" -> permissionManagementPanel.refreshData();
            case "Users" -> userManagementPanel.refreshData();
            case "Audit Logs" -> auditLogPanel.refreshData();
            case "Audit Logs (Legacy)" -> legacyAuditLogsPanel.refreshData();
            case "Pending Requests" -> pendingRequestsPanel.refreshData();
            case "Dashboard" -> {
                if (dashboardPanel != null) {
                    dashboardPanel.refreshAsync("", "", null);
                }
            }
            default -> {
                // No refresh behavior needed.
            }
        }
    }

}