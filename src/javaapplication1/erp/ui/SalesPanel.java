package javaapplication1.erp.ui;

import javaapplication1.erp.report.SalesReportService;
import javaapplication1.erp.service.SalesService;
import javaapplication1.erp.service.InvoicePdfService;
import javaapplication1.erp.service.LookupService;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Vector;
import javaapplication1.erp.util.DBUtil;

public class SalesPanel extends JPanel {
    private final SalesService salesService = new SalesService();
    private final InvoicePdfService invoicePdfService = new InvoicePdfService();
    private final SalesReportService salesReportService = new SalesReportService();
    private final LookupService lookupService = new LookupService();

    private final JComboBox<LookupService.LookupRow> cmbCustomer = new JComboBox<>();
    private final JComboBox<LookupService.LookupRow> cmbProduct = new JComboBox<>();
    private final JTextField txtQty = new JTextField(8);
    private final JTextField txtUnitPrice = new JTextField(10);
    private final JButton btnAddLine = new JButton("Add Line");
    private final JButton btnRemoveLine = new JButton("Remove Line");
    private final JButton btnClearCart = new JButton("Clear Cart");
    private final JButton btnCreateSale = new JButton("Create Sale");
    private final JButton btnExportInvoice = new JButton("Export Invoice PDF");
    private final JButton btnExportSalesReport = new JButton("Export Sales Report");
    private final JButton btnCancelSale = new JButton("Cancel Selected Sale");
    private final JButton btnRefresh = new JButton("Refresh Orders");

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Product ID", "Qty", "Unit Price", "Line Total"}, 0);
    private final JTable cartTable = new JTable(cartModel);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Order ID", "Order No", "Customer ID", "Date", "Status", "Total"}, 0);
    private final JTable orderTable = new JTable(model);

    public SalesPanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(cartTable), new JScrollPane(orderTable));
        split.setDividerLocation(180);
        add(split, BorderLayout.CENTER);

        UIUtils.styleTable(cartTable);
        UIUtils.styleTable(orderTable);
        UIUtils.styleButton(btnAddLine);
        UIUtils.styleButton(btnRemoveLine);
        UIUtils.styleButton(btnClearCart);
        UIUtils.styleButton(btnCreateSale);
        UIUtils.styleButton(btnExportInvoice);
        UIUtils.styleButton(btnExportSalesReport);
        UIUtils.styleButton(btnCancelSale);
        UIUtils.styleButton(btnRefresh);
        UIUtils.styleComboBox(cmbCustomer);
        UIUtils.styleComboBox(cmbProduct);
        UIUtils.styleTextField(txtQty);
        UIUtils.styleTextField(txtUnitPrice);

        bind();
        loadLookups();
        loadOrders();
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

        cmbCustomer.setPreferredSize(new Dimension(190, cmbCustomer.getPreferredSize().height));
        cmbProduct.setPreferredSize(new Dimension(190, cmbProduct.getPreferredSize().height));
        txtQty.setPreferredSize(new Dimension(90, txtQty.getPreferredSize().height));
        txtUnitPrice.setPreferredSize(new Dimension(110, txtUnitPrice.getPreferredSize().height));
        txtQty.setMinimumSize(new Dimension(70, txtQty.getMinimumSize().height));
        txtUnitPrice.setMinimumSize(new Dimension(90, txtUnitPrice.getMinimumSize().height));

        gbc.gridx = 0;
        filterRow.add(new JLabel("Customer"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.24;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(cmbCustomer, gbc);

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
        filterRow.add(new JLabel("Unit Price"), gbc);
        gbc.gridx = 7;
        gbc.weightx = 0.16;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterRow.add(txtUnitPrice, gbc);

        gbc.gridx = 8;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterRow.add(Box.createHorizontalGlue(), gbc);

        JPanel actionGrid = new JPanel(new GridLayout(2, 4, 8, 8));
        actionGrid.add(btnAddLine);
        actionGrid.add(btnRemoveLine);
        actionGrid.add(btnClearCart);
        actionGrid.add(btnCreateSale);
        actionGrid.add(btnExportInvoice);
        actionGrid.add(btnExportSalesReport);
        actionGrid.add(btnCancelSale);
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
        btnCreateSale.addActionListener(e -> createSale());
        btnExportInvoice.addActionListener(e -> exportInvoice());
        btnExportSalesReport.addActionListener(e -> exportSalesReport());
        btnCancelSale.addActionListener(e -> cancelSale());
        btnRefresh.addActionListener(e -> loadOrders());
    }

    private void exportSalesReport() {
        String fromValue = JOptionPane.showInputDialog(this, "From date (YYYY-MM-DD):", LocalDate.now().minusDays(30));
        if (fromValue == null) {
            return;
        }
        String toValue = JOptionPane.showInputDialog(this, "To date (YYYY-MM-DD):", LocalDate.now());
        if (toValue == null) {
            return;
        }
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromValue.trim());
            to = LocalDate.parse(toValue.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (to.isBefore(from)) {
            JOptionPane.showMessageDialog(this, "To date must be after/from the from date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("sales-report-" + from + "-to-" + to + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                salesReportService.exportSalesRangePdf(from, to, chooser.getSelectedFile().getAbsolutePath());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(SalesPanel.this, "Sales report exported successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SalesPanel.this, "Report export failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void addLine() {
        try {
            LookupService.LookupRow product = (LookupService.LookupRow) cmbProduct.getSelectedItem();
            if (product == null) throw new IllegalArgumentException("Select a product");
            int productId = product.id;
            int qty = Integer.parseInt(txtQty.getText().trim());
            BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText().trim());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            cartModel.addRow(new Object[]{productId + " - " + product.label, qty, unitPrice, lineTotal});
            txtQty.setText("");
            txtUnitPrice.setText("");
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

    private void createSale() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                LookupService.LookupRow customer = (LookupService.LookupRow) cmbCustomer.getSelectedItem();
                if (customer == null) {
                    throw new IllegalArgumentException("Select a customer");
                }
                int customerId = customer.id;
                java.util.List<SalesService.LineItem> items = new java.util.ArrayList<>();
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int productId = parseId(String.valueOf(cartModel.getValueAt(i, 0)));
                    int qty = Integer.parseInt(String.valueOf(cartModel.getValueAt(i, 1)));
                    BigDecimal unitPrice = new BigDecimal(String.valueOf(cartModel.getValueAt(i, 2)));
                    items.add(new SalesService.LineItem(productId, qty, unitPrice));
                }
                return salesService.createSale(customerId, items);
            }

            @Override
            protected void done() {
                try {
                    Integer orderId = get();
                    cartModel.setRowCount(0);
                    loadOrders();
                    JOptionPane.showMessageDialog(SalesPanel.this, "Sale created. Order ID: " + orderId);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SalesPanel.this, "Sale failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void exportInvoice() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a sales order first");
            return;
        }
        int orderId = (int) model.getValueAt(row, 0);
        String preview;
        try {
            preview = invoicePdfService.buildSalesInvoicePreview(orderId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Preview failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextArea area = new JTextArea(preview, 18, 70);
        area.setEditable(false);
        int confirmPreview = JOptionPane.showConfirmDialog(this, new JScrollPane(area),
                "Invoice Preview", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (confirmPreview != JOptionPane.OK_OPTION) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("invoice-" + orderId + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                invoicePdfService.exportSalesInvoice(orderId, chooser.getSelectedFile().getAbsolutePath());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(SalesPanel.this, "Invoice exported successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SalesPanel.this, "Export failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void cancelSale() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a sales order first");
            return;
        }
        int orderId = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel selected sale and rollback stock?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                salesService.cancelSale(orderId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadOrders();
                    JOptionPane.showMessageDialog(SalesPanel.this, "Sale cancelled successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SalesPanel.this, "Cancel failed: " + ex.getMessage(),
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
            java.util.List<LookupService.LookupRow> customers;
            java.util.List<LookupService.LookupRow> products;

            @Override
            protected Void doInBackground() throws Exception {
                customers = lookupService.customers();
                products = lookupService.activeProducts();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cmbCustomer.removeAllItems();
                    for (LookupService.LookupRow c : customers) cmbCustomer.addItem(c);
                    cmbProduct.removeAllItems();
                    for (LookupService.LookupRow p : products) cmbProduct.addItem(p);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SalesPanel.this, "Lookup load failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadOrders() {
        SwingWorker<Vector<Vector<Object>>, Void> worker = new SwingWorker<Vector<Vector<Object>>, Void>() {
            @Override
            protected Vector<Vector<Object>> doInBackground() throws Exception {
                Vector<Vector<Object>> rows = new Vector<>();
                String sql = "SELECT id, order_no, customer_id, order_date, status, total_amount FROM sales_orders ORDER BY id DESC LIMIT 200";
                try (Connection connection = DBUtil.getConnection();
                     PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("id"));
                        row.add(rs.getString("order_no"));
                        row.add(rs.getInt("customer_id"));
                        row.add(rs.getDate("order_date"));
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
                    JOptionPane.showMessageDialog(SalesPanel.this, "Failed to load sales: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        loadLookups();
        loadOrders();
    }
}
