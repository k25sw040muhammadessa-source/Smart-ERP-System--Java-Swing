package javaapplication1.erp.ui;

import javaapplication1.erp.service.PaymentService;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * Payment posting screen for sales and purchase invoices.
 */
public class PaymentPanel extends JPanel {
    private final PaymentService paymentService = new PaymentService();
    private final JTextField txtAmount = new JTextField(10);
    private final JButton btnPostSalesPayment = new JButton("Post Sales Payment");
    private final JButton btnPostPurchasePayment = new JButton("Post Purchase Payment");
    private final JButton btnRefresh = new JButton("Refresh");
    private final DefaultTableModel salesModel = new DefaultTableModel(
            new Object[]{"ID", "Invoice No", "Order ID", "Amount Due", "Paid", "Status", "Date"}, 0);
    private final DefaultTableModel purchaseModel = new DefaultTableModel(
            new Object[]{"ID", "Invoice No", "Purchase ID", "Amount Due", "Paid", "Status", "Date"}, 0);
    private final JTable tblSales = new JTable(salesModel);
    private final JTable tblPurchase = new JTable(purchaseModel);

    public PaymentPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel();
        top.add(new JLabel("Amount"));
        top.add(txtAmount);
        top.add(btnPostSalesPayment);
        top.add(btnPostPurchasePayment);
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sales Invoices", new JScrollPane(tblSales));
        tabs.addTab("Purchase Invoices", new JScrollPane(tblPurchase));
        add(tabs, BorderLayout.CENTER);

        UIUtils.styleTable(tblSales);
        UIUtils.styleTable(tblPurchase);
        UIUtils.styleButton(btnPostSalesPayment);
        UIUtils.styleButton(btnPostPurchasePayment);
        UIUtils.styleButton(btnRefresh);

        btnRefresh.addActionListener(e -> load());
        btnPostSalesPayment.addActionListener(e -> postSalesPayment());
        btnPostPurchasePayment.addActionListener(e -> postPurchasePayment());
        load();
    }

    private void load() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<PaymentService.InvoiceRow> sales;
            List<PaymentService.InvoiceRow> purchases;

            @Override
            protected Void doInBackground() throws Exception {
                sales = paymentService.salesInvoices();
                purchases = paymentService.purchaseInvoices();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    salesModel.setRowCount(0);
                    for (PaymentService.InvoiceRow row : sales) {
                        salesModel.addRow(new Object[]{row.id, row.invoiceNo, row.refId, row.amountDue, row.paidAmount, row.paymentStatus, row.invoiceDate});
                    }
                    purchaseModel.setRowCount(0);
                    for (PaymentService.InvoiceRow row : purchases) {
                        purchaseModel.addRow(new Object[]{row.id, row.invoiceNo, row.refId, row.amountDue, row.paidAmount, row.paymentStatus, row.invoiceDate});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PaymentPanel.this, "Failed to load invoices: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void postSalesPayment() {
        int row = tblSales.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a sales invoice first");
            return;
        }
        int invoiceId = (int) salesModel.getValueAt(row, 0);
        post(() -> paymentService.postSalesPayment(invoiceId, new BigDecimal(txtAmount.getText().trim())));
    }

    private void postPurchasePayment() {
        int row = tblPurchase.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a purchase invoice first");
            return;
        }
        int invoiceId = (int) purchaseModel.getValueAt(row, 0);
        post(() -> paymentService.postPurchasePayment(invoiceId, new BigDecimal(txtAmount.getText().trim())));
    }

    private void post(ThrowingRunnable runnable) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                runnable.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    txtAmount.setText("");
                    load();
                    JOptionPane.showMessageDialog(PaymentPanel.this, "Payment posted.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PaymentPanel.this, "Payment failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public void refreshData() {
        load();
    }
}
