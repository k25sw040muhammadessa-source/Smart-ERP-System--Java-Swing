package javaapplication1.erp.ui;

import javaapplication1.erp.ui.theme.UIUtils;
import javaapplication1.erp.util.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * UnitListPanel: manage product units (e.g., PCS, KG, LTR).
 */
public class UnitListPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Symbol", "Active"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JTextField txtName = new JTextField(20);
    private final JTextField txtSymbol = new JTextField(8);
    private final JCheckBox chkActive = new JCheckBox("Active", true);
    private final JTextField txtSearch = new JTextField(18);
    private final JButton btnSearch = new JButton("Search");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");

    public UnitListPanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        UIUtils.styleTable(table);
        UIUtils.styleButton(btnSearch);
        UIUtils.styleButton(btnRefresh);
        UIUtils.styleButton(btnAdd);
        UIUtils.styleButton(btnUpdate);
        UIUtils.styleButton(btnDelete);

        bind();
        ensureUnitsTable();
        loadUnits("");
    }

    private JPanel buildTop() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.add(new JLabel("Search"));
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);
        return panel;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.add(new JLabel("Name"));
        panel.add(txtName);
        panel.add(new JLabel("Symbol"));
        panel.add(txtSymbol);
        panel.add(chkActive);
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        return panel;
    }

    private void bind() {
        btnSearch.addActionListener(e -> loadUnits(txtSearch.getText().trim()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUnits("");
        });
        btnAdd.addActionListener(e -> addUnit());
        btnUpdate.addActionListener(e -> updateUnit());
        btnDelete.addActionListener(e -> deleteUnit());
        table.getSelectionModel().addListSelectionListener(e -> onSelectRow());
    }

    private void ensureUnitsTable() {
        String ddl = "CREATE TABLE IF NOT EXISTS units (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(80) NOT NULL UNIQUE, " +
                "symbol VARCHAR(20) NOT NULL UNIQUE, " +
                "active TINYINT NOT NULL DEFAULT 1, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String seed = "INSERT IGNORE INTO units (name, symbol, active) VALUES " +
                "('Piece', 'PCS', 1), ('Kilogram', 'KG', 1), ('Liter', 'LTR', 1)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ddlStmt = connection.prepareStatement(ddl);
             PreparedStatement seedStmt = connection.prepareStatement(seed)) {
            ddlStmt.execute();
            seedStmt.executeUpdate();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Units table setup failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUnits(String term) {
        SwingWorker<Vector<Vector<Object>>, Void> worker = new SwingWorker<Vector<Vector<Object>>, Void>() {
            @Override
            protected Vector<Vector<Object>> doInBackground() throws Exception {
                Vector<Vector<Object>> rows = new Vector<>();
                String sql = "SELECT id, name, symbol, active FROM units " +
                        "WHERE name LIKE ? OR symbol LIKE ? ORDER BY name ASC";
                String q = "%" + term + "%";
                try (Connection connection = DBUtil.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, q);
                    statement.setString(2, q);
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            Vector<Object> row = new Vector<>();
                            row.add(rs.getInt("id"));
                            row.add(rs.getString("name"));
                            row.add(rs.getString("symbol"));
                            row.add(rs.getBoolean("active"));
                            rows.add(row);
                        }
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
                    JOptionPane.showMessageDialog(UnitListPanel.this, "Failed to load units: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void addUnit() {
        String name = txtName.getText().trim();
        String symbol = txtSymbol.getText().trim().toUpperCase();
        if (name.isEmpty() || symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Symbol are required.");
            return;
        }
        String sql = "INSERT INTO units (name, symbol, active) VALUES (?, ?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, symbol);
            statement.setBoolean(3, chkActive.isSelected());
            statement.executeUpdate();
            clearForm();
            loadUnits(txtSearch.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUnit() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a unit first.");
            return;
        }
        String name = txtName.getText().trim();
        String symbol = txtSymbol.getText().trim().toUpperCase();
        if (name.isEmpty() || symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Symbol are required.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE units SET name = ?, symbol = ?, active = ? WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, symbol);
            statement.setBoolean(3, chkActive.isSelected());
            statement.setInt(4, id);
            statement.executeUpdate();
            clearForm();
            loadUnits(txtSearch.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUnit() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a unit first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected unit?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String sql = "DELETE FROM units WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            clearForm();
            loadUnits(txtSearch.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSelectRow() {
        int row = selectedModelRow();
        if (row < 0) {
            return;
        }
        txtName.setText(String.valueOf(model.getValueAt(row, 1)));
        txtSymbol.setText(String.valueOf(model.getValueAt(row, 2)));
        chkActive.setSelected(Boolean.parseBoolean(String.valueOf(model.getValueAt(row, 3))));
    }

    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    private void clearForm() {
        txtName.setText("");
        txtSymbol.setText("");
        chkActive.setSelected(true);
        table.clearSelection();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        loadUnits(txtSearch.getText().trim());
    }

    public void refreshData() {
        txtSearch.setText("");
        loadUnits("");
    }
}
