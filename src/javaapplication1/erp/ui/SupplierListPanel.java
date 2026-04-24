package javaapplication1.erp.ui;

import javaapplication1.erp.dao.impl.SupplierDAOImpl;
import javaapplication1.erp.model.Supplier;
import javaapplication1.erp.service.SupplierService;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.AnimatedPanel;
import javaapplication1.erp.ui.theme.PlaceholderTextField;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierListPanel extends JPanel {
    private final SupplierService service = new SupplierService(new SupplierDAOImpl());
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID","Name","Contact","Phone","Email"}, 0);
    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new PlaceholderTextField("e.g. Global Supplies Ltd", 20);
    private final JTextField txtContact = new PlaceholderTextField("e.g. Mark Johnson", 20);
    private final JTextField txtPhone = new PlaceholderTextField("e.g. +1 234 567 890", 12);
    private final JTextField txtEmail = new PlaceholderTextField("e.g. supplier@example.com", 20);
    private final JTextField txtAddress = new PlaceholderTextField("e.g. 123 Business Avenue", 20);
    private final JTextField txtSearch = new JTextField(20);

    private final JButton btnSearch = new JButton("Search");
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");

    public SupplierListPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.BACKGROUND);
        
        // Create animated form container
        JPanel formContent = buildForm();
        AnimatedPanel animatedForm = new AnimatedPanel(new BorderLayout());
        animatedForm.setPreferredSize(new Dimension(460, 560));
        animatedForm.setBorder(new EmptyBorder(8, 8, 8, 8));
        animatedForm.add(formContent, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animatedForm, buildTable());
        split.setDividerLocation(460);
        add(split, BorderLayout.CENTER);
            // Apply styling
            UIUtils.styleTable(table);
            UIUtils.styleButton(btnAdd);
            UIUtils.styleButton(btnUpdate);
            UIUtils.styleButton(btnDelete);
            UIUtils.styleButton(btnSearch);
            UIUtils.styleTextField(txtName);
            UIUtils.styleTextField(txtContact);
            UIUtils.styleTextField(txtPhone);
            UIUtils.styleTextField(txtEmail);
            UIUtils.styleTextField(txtAddress);
            UIUtils.styleTextField(txtSearch);
            bindActions();
            loadSuppliers("");
    }

    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 23, 42, 25));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 10, 18, 18);

                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 16, 16);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, 16, 16);

                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(24, 28, 24, 28));
        cardPanel.setPreferredSize(new Dimension(410, 530));

        JLabel titleLabel = new JLabel("Supplier Details");
        titleLabel.setFont(Theme.FONT_SUBHEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        cardPanel.add(createFieldBlock("Name", txtName));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Contact Name", txtContact));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Phone", txtPhone));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Email", txtEmail));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Address", txtAddress));
        cardPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdd.setFont(Theme.FONT_BODY_MEDIUM);
        btnAdd.setPreferredSize(new Dimension(90, 38));
        btnUpdate.setFont(Theme.FONT_BODY_MEDIUM);
        btnUpdate.setPreferredSize(new Dimension(90, 38));
        btnDelete.setFont(Theme.FONT_BODY_MEDIUM);
        btnDelete.setPreferredSize(new Dimension(90, 38));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        cardPanel.add(buttonPanel);

        wrapper.add(cardPanel, new GridBagConstraints());
        return wrapper;
    }

    private JPanel createFieldBlock(String labelText, JTextField field) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(Component.CENTER_ALIGNMENT);
        block.setMaximumSize(new Dimension(330, 62));

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY_MEDIUM);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(Theme.FONT_BODY);
        field.setPreferredSize(new Dimension(320, 36));
        field.setMaximumSize(new Dimension(320, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    private JPanel buildTable() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        top.add(txtSearch);
        top.add(btnSearch);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void bindActions() {
        btnSearch.addActionListener(e -> loadSuppliers(txtSearch.getText().trim()));
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtName.setText(valueAt(row, 1));
                txtContact.setText(valueAt(row, 2));
                txtPhone.setText(valueAt(row, 3));
                txtEmail.setText(valueAt(row, 4));
            }
        });
    }

    private String valueAt(int row, int col) {
        Object value = tableModel.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private Supplier readForm() {
        Supplier supplier = new Supplier();
        supplier.setName(txtName.getText().trim());
        supplier.setContactName(txtContact.getText().trim());
        supplier.setPhone(txtPhone.getText().trim());
        supplier.setEmail(txtEmail.getText().trim());
        supplier.setAddress(txtAddress.getText().trim());
        return supplier;
    }

    private void loadSuppliers(String term) {
        SwingWorker<List<Supplier>, Void> worker = new SwingWorker<List<Supplier>, Void>() {
            @Override
            protected List<Supplier> doInBackground() throws Exception {
                return service.search(term, 200, 0);
            }

            @Override
            protected void done() {
                try {
                    List<Supplier> suppliers = get();
                    tableModel.setRowCount(0);
                    for (Supplier supplier : suppliers) {
                        tableModel.addRow(new Object[]{
                                supplier.getId(),
                                supplier.getName(),
                                supplier.getContactName(),
                                supplier.getPhone(),
                                supplier.getEmail()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SupplierListPanel.this, "Failed to load suppliers: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onAdd() {
        Supplier supplier = readForm();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.createSupplier(supplier);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadSuppliers("");
                    JOptionPane.showMessageDialog(SupplierListPanel.this, "Supplier added");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SupplierListPanel.this, "Add failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier first");
            return;
        }
        Supplier supplier = readForm();
        supplier.setId((int) tableModel.getValueAt(row, 0));

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return service.updateSupplier(supplier);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadSuppliers("");
                        JOptionPane.showMessageDialog(SupplierListPanel.this, "Supplier updated");
                    } else {
                        JOptionPane.showMessageDialog(SupplierListPanel.this, "No changes were saved");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SupplierListPanel.this, "Update failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier first");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected supplier?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        int supplierId = (int) tableModel.getValueAt(row, 0);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return new SupplierDAOImpl().delete(supplierId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadSuppliers("");
                        JOptionPane.showMessageDialog(SupplierListPanel.this, "Supplier deleted");
                    } else {
                        JOptionPane.showMessageDialog(SupplierListPanel.this, "Delete failed");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SupplierListPanel.this, "Delete failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        loadSuppliers(txtSearch.getText().trim());
    }

    public void refreshData() {
        txtSearch.setText("");
        loadSuppliers("");
    }
}
