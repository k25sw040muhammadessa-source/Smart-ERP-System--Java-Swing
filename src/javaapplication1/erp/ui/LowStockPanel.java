package javaapplication1.erp.ui;

import javaapplication1.erp.report.InventoryReportService;
import javaapplication1.erp.ui.theme.UIUtils;
import javaapplication1.erp.util.DBUtil;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * Low stock management view with reorder suggestion.
 */
public class LowStockPanel extends JPanel {
    private final InventoryReportService inventoryReportService = new InventoryReportService();
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnExportPdf = new JButton("Export Low Stock PDF");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Product ID", "SKU", "Name", "Stock", "Reorder Level", "Suggested Reorder Qty"}, 0);
    private final JTable table = new JTable(model);

    public LowStockPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel();
        top.add(btnRefresh);
        top.add(btnExportPdf);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        UIUtils.styleTable(table);
        UIUtils.styleButton(btnRefresh);
        UIUtils.styleButton(btnExportPdf);

        btnRefresh.addActionListener(e -> load());
        btnExportPdf.addActionListener(e -> exportPdf());
        load();
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("low-stock-report.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                inventoryReportService.exportLowStockPdf(chooser.getSelectedFile().getAbsolutePath());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(LowStockPanel.this, "Low stock report exported successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LowStockPanel.this, "Export failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void load() {
        SwingWorker<Vector<Vector<Object>>, Void> worker = new SwingWorker<Vector<Vector<Object>>, Void>() {
            @Override
            protected Vector<Vector<Object>> doInBackground() throws Exception {
                Vector<Vector<Object>> rows = new Vector<>();
                String sql = "SELECT id, sku, name, qty_in_stock, reorder_level FROM products " +
                        "WHERE active = 1 AND qty_in_stock <= reorder_level ORDER BY qty_in_stock ASC";
                try (Connection c = DBUtil.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int stock = rs.getInt("qty_in_stock");
                        int reorder = rs.getInt("reorder_level");
                        int suggested = Math.max(reorder * 2 - stock, reorder);
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("id"));
                        row.add(rs.getString("sku"));
                        row.add(rs.getString("name"));
                        row.add(stock);
                        row.add(reorder);
                        row.add(suggested);
                        rows.add(row);
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    Vector<Vector<Object>> rows = get();
                    model.setRowCount(0);
                    for (Vector<Object> row : rows) {
                        model.addRow(row);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LowStockPanel.this, "Failed to load low-stock list: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        load();
    }
}
