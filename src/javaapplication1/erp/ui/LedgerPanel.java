package javaapplication1.erp.ui;

import javaapplication1.erp.service.LedgerService;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Displays customer and supplier ledgers.
 */
public class LedgerPanel extends JPanel {
    private final LedgerService ledgerService = new LedgerService();
    private final JButton btnRefresh = new JButton("Refresh");
    private final DefaultTableModel customerModel = new DefaultTableModel(
            new Object[]{"Customer ID", "Name", "Total Invoiced", "Total Paid", "Balance"}, 0);
    private final DefaultTableModel supplierModel = new DefaultTableModel(
            new Object[]{"Supplier ID", "Name", "Total Invoiced", "Total Paid", "Balance"}, 0);
    private final JTable customerTable = new JTable(customerModel);
    private final JTable supplierTable = new JTable(supplierModel);

    public LedgerPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel();
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customer Ledger", new JScrollPane(customerTable));
        tabs.addTab("Supplier Ledger", new JScrollPane(supplierTable));
        add(tabs, BorderLayout.CENTER);

        UIUtils.styleTable(customerTable);
        UIUtils.styleTable(supplierTable);
        UIUtils.styleButton(btnRefresh);

        btnRefresh.addActionListener(e -> load());
        load();
    }

    private void load() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<LedgerService.LedgerRow> customers;
            List<LedgerService.LedgerRow> suppliers;

            @Override
            protected Void doInBackground() throws Exception {
                customers = ledgerService.customerLedger();
                suppliers = ledgerService.supplierLedger();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    customerModel.setRowCount(0);
                    for (LedgerService.LedgerRow row : customers) {
                        customerModel.addRow(new Object[]{row.partyId, row.partyName, row.totalInvoiced, row.totalPaid, row.balance});
                    }
                    supplierModel.setRowCount(0);
                    for (LedgerService.LedgerRow row : suppliers) {
                        supplierModel.addRow(new Object[]{row.partyId, row.partyName, row.totalInvoiced, row.totalPaid, row.balance});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LedgerPanel.this, "Failed to load ledgers: " + ex.getMessage(),
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
