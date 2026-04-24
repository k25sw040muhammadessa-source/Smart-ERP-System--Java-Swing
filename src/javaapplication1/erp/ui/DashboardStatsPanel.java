package javaapplication1.erp.ui;

import javaapplication1.erp.service.DashboardService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.FlowLayout;
import java.util.Map;

/**
 * Simple KPI row for the dashboard header.
 */
public class DashboardStatsPanel extends JPanel {
    private final DashboardService dashboardService = new DashboardService();

    private final JLabel lblProducts = new JLabel("Products: 0");
    private final JLabel lblCustomers = new JLabel("Customers: 0");
    private final JLabel lblSuppliers = new JLabel("Suppliers: 0");
    private final JLabel lblTodaySales = new JLabel("Today Sales: 0.00");
    private final JLabel lblLowStock = new JLabel("Low Stock: 0");
    private boolean lowStockWarned;

    public DashboardStatsPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));
        add(lblProducts);
        add(lblCustomers);
        add(lblSuppliers);
        add(lblTodaySales);
        add(lblLowStock);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadKpis());
        add(btnRefresh);
        loadKpis();
    }

    private void loadKpis() {
        SwingWorker<Map<String, String>, Void> worker = new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() {
                return dashboardService.fetchKpis();
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> kpis = get();
                    lblProducts.setText("Products: " + kpis.get("products"));
                    lblCustomers.setText("Customers: " + kpis.get("customers"));
                    lblSuppliers.setText("Suppliers: " + kpis.get("suppliers"));
                    lblTodaySales.setText("Today Sales: " + kpis.get("todaySales"));
                    int lowStockCount = Integer.parseInt(kpis.get("lowStockCount"));
                    lblLowStock.setText("Low Stock: " + lowStockCount);
                    if (lowStockCount > 0 && !lowStockWarned) {
                        lowStockWarned = true;
                        javax.swing.JOptionPane.showMessageDialog(
                                DashboardStatsPanel.this,
                                "Low stock alert: " + lowStockCount + " product(s) are at or below reorder level.",
                                "Inventory Alert",
                                javax.swing.JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (Exception ignored) {
                    // Keep old values if refresh fails.
                }
            }
        };
        worker.execute();
    }
}
