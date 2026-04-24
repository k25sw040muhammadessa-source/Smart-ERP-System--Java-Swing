package javaapplication1.erp.ui;

import javaapplication1.erp.service.PurchaseService;
import javaapplication1.erp.service.LookupService;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javaapplication1.erp.util.DBUtil;

public class PurchasePanel extends JPanel {
    private final PurchaseService purchaseService = new PurchaseService();
    private final LookupService lookupService = new LookupService();

    private final JComboBox<LookupService.LookupRow> cmbSupplier = new JComboBox<>();
    private final JComboBox<LookupService.LookupRow> cmbProduct = new JComboBox<>();
    private final JTextField txtQty = new JTextField(8);
    private final JTextField txtUnitCost = new JTextField(10);
    private final JButton btnAddLine = new JButton("Add Line");
    private final JButton btnRemoveLine = new JButton("Remove Line");
    private final JButton btnClearCart = new JButton("Clear Cart");
    private final JButton btnCreatePurchase = new JButton("Create Purchase");
    private final JButton btnCancelPurchase = new JButton("Cancel Selected Purchase");
    private final JButton btnRefresh = new JButton("Refresh Purchases");
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Product ID", "Qty", "Unit Cost", "Line Total"}, 0);
    private final JTable cartTable = new JTable(cartModel);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Purchase ID", "Purchase No", "Supplier ID", "Date", "Status", "Total"}, 0);
    private final JTable purchaseTable = new JTable(model);

    public PurchasePanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(cartTable), new JScrollPane(purchaseTable));
        split.setDividerLocation(180);
        add(split, BorderLayout.CENTER);

        UIUtils.styleTable(cartTable);
        UIUtils.styleTable(purchaseTable);
        UIUtils.styleButton(btnAddLine);
        UIUtils.styleButton(btnRemoveLine);
        UIUtils.styleButton(btnClearCart);
        UIUtils.styleButton(btnCreatePurchase);
        UIUtils.styleButton(btnCancelPurchase);
        UIUtils.styleButton(btnRefresh);
        UIUtils.styleComboBox(cmbSupplier);
        UIUtils.styleComboBox(cmbProduct);
        UIUtils.styleTextField(txtQty);
        UIUtils.styleTextField(txtUnitCost);

        bind();
        loadLookups();
        loadPurchases();
    }

    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 14, 10));

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        cmbSupplier.setPreferredSize(new Dimension(190, cmbSupplier.getPreferredSize().height));
        cmbProduct.setPreferredSize(new Dimension(190, cmbProduct.getPreferredSize().height));
        txtQty.setPreferredSize(new Dimension(90, txtQty.getPreferredSize().height));
        txtUnitCost.setPreferredSize(new Dimension(110, txtUnitCost.getPreferredSize().height));
        txtQty.setMinimumSize(new Dimension(70, txtQty.getMinimumSize().height));
        txtUnitCost.setMinimumSize(new Dimension(90, txtUnitCost.getMinimumSize().height));

        gbc.gridx = 0;
        filterRow.add(new JLabel("Supplier"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.24;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(cmbSupplier, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filterRow.add(new JLabel("Product"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.24;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(cmbProduct, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filterRow.add(new JLabel("Quantity"), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.14;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(txtQty, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filterRow.add(new JLabel("Unit Cost"), gbc);
        gbc.gridx = 7;
        gbc.weightx = 0.16;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(txtUnitCost, gbc);

        gbc.gridx = 8;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(Box.createHorizontalGlue(), gbc);

        JPanel actionGrid = new JPanel(new GridLayout(2, 3, 8, 8));
        actionGrid.add(btnAddLine);
        actionGrid.add(btnRemoveLine);
        actionGrid.add(btnClearCart);
        actionGrid.add(btnCreatePurchase);
        actionGrid.add(btnCancelPurchase);
        actionGrid.add(btnRefresh);

        top.add(filterRow);
        top.add(Box.createVerticalStrut(4));
        top.add(actionGrid);
        return top;
    }

    private void bind() {
        btnAddLine.addActionListener(e -> addLine());
        btnRemoveLine.addActionListener(e -> removeLine());
        btnClearCart.addActionListener(e -> cartModel.setRowCount(0));
        btnCreatePurchase.addActionListener(e -> createPurchase());
        btnCancelPurchase.addActionListener(e -> cancelPurchase());
        btnRefresh.addActionListener(e -> loadPurchases());
    }

    private void addLine() {
        try {
            LookupService.LookupRow product = (LookupService.LookupRow) cmbProduct.getSelectedItem();
            if (product == null) throw new IllegalArgumentException("Select a product");
            int productId = product.id;
            int qty = Integer.parseInt(txtQty.getText().trim());
            BigDecimal unitCost = new BigDecimal(txtUnitCost.getText().trim());
            BigDecimal lineTotal = unitCost.multiply(BigDecimal.valueOf(qty));
            cartModel.addRow(new Object[]{productId + " - " + product.label, qty, unitCost, lineTotal});
            txtQty.setText("");
            txtUnitCost.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid line item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeLine() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cartModel.removeRow(row);
        }
    }

    private void createPurchase() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                LookupService.LookupRow supplier = (LookupService.LookupRow) cmbSupplier.getSelectedItem();
                if (supplier == null) throw new IllegalArgumentException("Select a supplier");
                int supplierId = supplier.id;
                java.util.List<PurchaseService.LineItem> items = new java.util.ArrayList<>();
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int productId = parseId(String.valueOf(cartModel.getValueAt(i, 0)));
                    int qty = Integer.parseInt(String.valueOf(cartModel.getValueAt(i, 1)));
                    BigDecimal unitCost = new BigDecimal(String.valueOf(cartModel.getValueAt(i, 2)));
                    items.add(new PurchaseService.LineItem(productId, qty, unitCost));
                }
                return purchaseService.createPurchase(supplierId, items);
            }

            @Override
            protected void done() {
                try {
                    Integer purchaseId = get();
                    cartModel.setRowCount(0);
                    loadPurchases();
                    JOptionPane.showMessageDialog(PurchasePanel.this, "Purchase created. ID: " + purchaseId);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PurchasePanel.this, "Purchase failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private int parseId(String value) {
        int idx = value.indexOf(" - ");
        return Integer.parseInt(idx > 0 ? value.substring(0, idx) : value);
    }

    private void loadLookups() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            java.util.List<LookupService.LookupRow> suppliers;
            java.util.List<LookupService.LookupRow> products;

            @Override
            protected Void doInBackground() throws Exception {
                suppliers = lookupService.suppliers();
                products = lookupService.activeProducts();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cmbSupplier.removeAllItems();
                    for (LookupService.LookupRow s : suppliers) cmbSupplier.addItem(s);
                    cmbProduct.removeAllItems();
                    for (LookupService.LookupRow p : products) cmbProduct.addItem(p);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PurchasePanel.this, "Lookup load failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadPurchases() {
        SwingWorker<Vector<Vector<Object>>, Void> worker = new SwingWorker<Vector<Vector<Object>>, Void>() {
            @Override
            protected Vector<Vector<Object>> doInBackground() throws Exception {
                Vector<Vector<Object>> rows = new Vector<>();
                String sql = "SELECT id, purchase_no, supplier_id, purchase_date, status, total_amount FROM purchases ORDER BY id DESC LIMIT 200";
                try (Connection connection = DBUtil.getConnection();
                     PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("id"));
                        row.add(rs.getString("purchase_no"));
                        row.add(rs.getInt("supplier_id"));
                        row.add(rs.getDate("purchase_date"));
                        row.add(rs.getString("status"));
                        row.add(rs.getBigDecimal("total_amount"));
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
                    JOptionPane.showMessageDialog(PurchasePanel.this, "Failed to load purchases: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void cancelPurchase() {
        int row = purchaseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a purchase first");
            return;
        }
        int purchaseId = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel selected purchase and rollback stock?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                purchaseService.cancelPurchase(purchaseId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadPurchases();
                    JOptionPane.showMessageDialog(PurchasePanel.this, "Purchase cancelled successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PurchasePanel.this, "Cancel failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        loadLookups();
        loadPurchases();
    }
}
