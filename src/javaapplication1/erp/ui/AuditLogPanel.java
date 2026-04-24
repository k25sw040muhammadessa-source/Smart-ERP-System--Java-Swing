package javaapplication1.erp.ui;

import javaapplication1.erp.service.AuditLogService;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Admin panel to browse audit logs.
 */
public class AuditLogPanel extends JPanel {
    private final AuditLogService service = new AuditLogService();
    private final JTextField txtSearch = new JTextField(24);
    private final JButton btnSearch = new JButton("Search");
    private final JButton btnRefresh = new JButton("Refresh");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "User ID", "Action", "Entity", "Entity ID", "Details", "Created At"}, 0);
    private final JTable table = new JTable(model);

    public AuditLogPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel();
        top.add(new JLabel("Keyword"));
        top.add(txtSearch);
        top.add(btnSearch);
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        UIUtils.styleTable(table);
        UIUtils.styleButton(btnSearch);
        UIUtils.styleButton(btnRefresh);

        btnSearch.addActionListener(e -> load());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            load();
        });
        load();
    }

    private void load() {
        SwingWorker<List<AuditLogService.AuditLogRow>, Void> worker =
                new SwingWorker<List<AuditLogService.AuditLogRow>, Void>() {
            @Override
            protected List<AuditLogService.AuditLogRow> doInBackground() throws Exception {
                return service.search(txtSearch.getText().trim(), 500);
            }

            @Override
            protected void done() {
                try {
                    List<AuditLogService.AuditLogRow> rows = get();
                    model.setRowCount(0);
                    for (AuditLogService.AuditLogRow row : rows) {
                        model.addRow(new Object[]{
                                row.id, row.userId, row.action, row.entityName, row.entityId, row.details, row.createdAt
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AuditLogPanel.this, "Failed to load logs: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        load();
    }

    public void refreshData() {
        txtSearch.setText("");
        load();
    }
}
