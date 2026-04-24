package javaapplication1.erp.ui;

import javaapplication1.erp.service.InventoryService;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javaapplication1.erp.util.DBUtil;

public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService = new InventoryService();
    private final JTextField txtProductId = new JTextField(8);
    private final JTextField txtQty = new JTextField(8);
    private final JTextField txtNote = new JTextField(24);
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"IN", "OUT"});
    private final JButton btnPost = new JButton("Post Transaction");
    private final JButton btnRefresh = new JButton("Refresh Ledger");
    private final DefaultTableModel ledgerModel =
            new DefaultTableModel(new Object[]{"ID", "Product ID", "Type", "Qty", "Ref", "Date", "Note"}, 0);
    private final JTable ledgerTable = new JTable(ledgerModel);

    public InventoryPanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(ledgerTable), BorderLayout.CENTER);

        UIUtils.styleTable(ledgerTable);
        UIUtils.styleButton(btnPost);
        UIUtils.styleButton(btnRefresh);

        bind();
        loadLedger();
    }

    private JPanel buildTop() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 14, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtProductId.setPreferredSize(new Dimension(90, txtProductId.getPreferredSize().height));
        txtQty.setPreferredSize(new Dimension(90, txtQty.getPreferredSize().height));
        txtNote.setPreferredSize(new Dimension(220, txtNote.getPreferredSize().height));
        txtNote.setMinimumSize(new Dimension(180, txtNote.getMinimumSize().height));

        gbc.gridx = 0;
        panel.add(new JLabel("Product ID"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtProductId, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Type"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmbType, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Quantity"), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtQty, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Note"), gbc);
        gbc.gridx = 7;
        gbc.weightx = 0.36;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtNote, gbc);

        gbc.gridx = 8;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnPost, gbc);
        gbc.gridx = 9;
        panel.add(btnRefresh, gbc);

        gbc.gridx = 10;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);
        return panel;
    }

    private void bind() {
        btnPost.addActionListener(e -> postTransaction());
        btnRefresh.addActionListener(e -> loadLedger());
    }

    private void postTransaction() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                int productId = Integer.parseInt(txtProductId.getText().trim());
                int qty = Integer.parseInt(txtQty.getText().trim());
                String type = String.valueOf(cmbType.getSelectedItem());
                if ("IN".equals(type)) {
                    inventoryService.stockIn(productId, qty, txtNote.getText().trim());
                } else {
                    inventoryService.stockOut(productId, qty, txtNote.getText().trim());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadLedger();
                    JOptionPane.showMessageDialog(InventoryPanel.this, "Inventory transaction posted");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InventoryPanel.this, "Failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadLedger() {
        SwingWorker<Vector<Vector<Object>>, Void> worker = new SwingWorker<Vector<Vector<Object>>, Void>() {
            @Override
            protected Vector<Vector<Object>> doInBackground() throws Exception {
                Vector<Vector<Object>> rows = new Vector<>();
                String sql = "SELECT id, product_id, type, qty, ref_type, movement_date, note " +
                        "FROM inventory_movements ORDER BY id DESC LIMIT 200";
                try (Connection connection = DBUtil.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql);
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("id"));
                        row.add(rs.getInt("product_id"));
                        row.add(rs.getString("type"));
                        row.add(rs.getInt("qty"));
                        row.add(rs.getString("ref_type"));
                        row.add(rs.getTimestamp("movement_date"));
                        row.add(rs.getString("note"));
                        rows.add(row);
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    Vector<Vector<Object>> rows = get();
                    ledgerModel.setRowCount(0);
                    for (Vector<Object> row : rows) {
                        ledgerModel.addRow(row);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InventoryPanel.this, "Failed to load ledger: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        loadLedger();
    }
}
