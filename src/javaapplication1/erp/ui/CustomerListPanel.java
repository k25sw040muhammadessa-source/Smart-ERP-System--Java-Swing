package javaapplication1.erp.ui;

import javaapplication1.erp.dao.impl.CustomerDAOImpl;
import javaapplication1.erp.model.Customer;
import javaapplication1.erp.service.CustomerService;
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

/**
 * CustomerListPanel: form + table for customer CRUD and search.
 */
public class CustomerListPanel extends JPanel {
    private final CustomerService service = new CustomerService(new CustomerDAOImpl());
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID","Name","Contact","Phone","Email"}, 0);
    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new PlaceholderTextField("e.g. Acme Corporation", 20);
    private final JTextField txtContact = new PlaceholderTextField("e.g. John Smith", 20);
    private final JTextField txtPhone = new PlaceholderTextField("e.g. +1 234 567 890", 12);
    private final JTextField txtEmail = new PlaceholderTextField("e.g. customer@example.com", 20);
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JTextField txtSearch = new JTextField(20);
    private final JButton btnSearch = new JButton("Search");

    public CustomerListPanel() {
        setLayout(new BorderLayout(8,8));
        setBackground(Theme.BACKGROUND);
        
        // Create animated form container
        JPanel formContent = buildForm();
        AnimatedPanel animatedForm = new AnimatedPanel(new BorderLayout());
        animatedForm.setPreferredSize(new Dimension(460, 560));
        animatedForm.setBorder(new EmptyBorder(8, 8, 8, 8));
        animatedForm.add(formContent, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animatedForm, buildTablePanel());
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
            UIUtils.styleTextField(txtSearch);
            loadCustomers();
            bindActions();
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

        JLabel titleLabel = new JLabel("Customer Details");
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

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(); top.add(txtSearch); top.add(btnSearch);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void bindActions() {
        btnSearch.addActionListener(e -> loadCustomers(txtSearch.getText().trim()));
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                txtName.setText((String) tableModel.getValueAt(r,1));
                txtContact.setText((String) tableModel.getValueAt(r,2));
                txtPhone.setText((String) tableModel.getValueAt(r,3));
                txtEmail.setText((String) tableModel.getValueAt(r,4));
            }
        });
    }

    private void loadCustomers() { loadCustomers(""); }

    private void loadCustomers(String term) {
        SwingWorker<List<Customer>, Void> worker = new SwingWorker<List<Customer>, Void>() {
            @Override protected List<Customer> doInBackground() throws Exception { return service.search(term, 200, 0); }
            @Override protected void done() {
                try {
                    List<Customer> list = get();
                    tableModel.setRowCount(0);
                    for (Customer c : list) tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getContactName(), c.getPhone(), c.getEmail()});
                } catch (Exception ex) { JOptionPane.showMessageDialog(CustomerListPanel.this, "Failed to load customers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }

    private void onAdd() {
        try {
            Customer c = new Customer();
            c.setName(txtName.getText().trim());
            c.setContactName(txtContact.getText().trim());
            c.setPhone(txtPhone.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            service.createCustomer(c);
            loadCustomers();
            JOptionPane.showMessageDialog(this, "Customer added");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void onUpdate() {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a customer first"); return; }
        try {
            Customer c = new Customer();
            c.setId((int) tableModel.getValueAt(r,0));
            c.setName(txtName.getText().trim());
            c.setContactName(txtContact.getText().trim());
            c.setPhone(txtPhone.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            service.updateCustomer(c);
            loadCustomers();
            JOptionPane.showMessageDialog(this, "Customer updated");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void onDelete() {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a customer first"); return; }
        int id = (int) tableModel.getValueAt(r,0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected customer?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return new javaapplication1.erp.dao.impl.CustomerDAOImpl().delete(id); }
            @Override protected void done() {
                try { if (get()) { loadCustomers(); JOptionPane.showMessageDialog(CustomerListPanel.this, "Deleted"); } else JOptionPane.showMessageDialog(CustomerListPanel.this, "Delete failed"); }
                catch (Exception ex) { JOptionPane.showMessageDialog(CustomerListPanel.this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        loadCustomers(txtSearch.getText().trim());
    }

    public void refreshData() {
        txtSearch.setText("");
        loadCustomers("");
    }
}
