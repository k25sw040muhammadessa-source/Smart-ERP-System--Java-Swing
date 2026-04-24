package javaapplication1.erp.ui;

import javaapplication1.erp.dao.impl.ProductDAOImpl;
import javaapplication1.erp.model.Product;
import javaapplication1.erp.service.ProductService;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.UIUtils;
import javaapplication1.erp.ui.theme.AnimatedPanel;
import javaapplication1.erp.ui.theme.PlaceholderTextField;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * ProductListPanel: left form for add/edit, right table for list/search.
 */
public class ProductListPanel extends JPanel {
    private final ProductService service = new ProductService(new ProductDAOImpl());
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID","SKU","Name","Price","Stock"}, 0);
    private final JTable table = new JTable(tableModel);

    private final JTextField txtSku = new PlaceholderTextField("e.g. PROD001", 15);
    private final JTextField txtName = new PlaceholderTextField("e.g. Laptop", 15);
    private final JTextField txtPrice = new PlaceholderTextField("e.g. 1500.00", 8);
    private final JTextField txtQty = new PlaceholderTextField("e.g. 50", 6);
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JTextField txtSearch = new JTextField(20);
    private final JButton btnSearch = new JButton("Search");

    public ProductListPanel() {
        setLayout(new BorderLayout(8,8));
        setBackground(Theme.BACKGROUND);
        
        // Create animated form container
        JPanel formContent = buildForm();
        AnimatedPanel animatedForm = new AnimatedPanel(new BorderLayout());
        animatedForm.setPreferredSize(new Dimension(460, 460));
        animatedForm.setBorder(new EmptyBorder(8, 8, 8, 8));
        animatedForm.add(formContent, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animatedForm, buildTablePanel());
        split.setDividerLocation(460);
        add(split, BorderLayout.CENTER);
        UIUtils.styleTable(table);
        UIUtils.styleButton(btnAdd);
        UIUtils.styleButton(btnUpdate);
        UIUtils.styleButton(btnDelete);
        UIUtils.styleButton(btnSearch);
        enforceSingleRowButtonSizes();
        UIUtils.styleTextField(txtSku);
        UIUtils.styleTextField(txtName);
        UIUtils.styleTextField(txtPrice);
        UIUtils.styleTextField(txtQty);
        UIUtils.styleTextField(txtSearch);
        loadProducts();
        bindActions();
    }

    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

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
        cardPanel.setPreferredSize(new Dimension(410, 430));

        JLabel titleLabel = new JLabel("Product Details");
        titleLabel.setFont(Theme.FONT_SUBHEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        cardPanel.add(createFieldBlock("SKU", txtSku));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Name", txtName));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Price", txtPrice));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Qty in Stock", txtQty));
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        wrapper.add(cardPanel, gbc);
        return wrapper;
    }

    private void enforceSingleRowButtonSizes() {
        Dimension formButtonSize = new Dimension(96, 38);
        JButton[] formButtons = {btnAdd, btnUpdate, btnDelete};
        for (JButton button : formButtons) {
            button.setPreferredSize(formButtonSize);
            button.setMinimumSize(formButtonSize);
            button.setMaximumSize(formButtonSize);
        }
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
        btnSearch.addActionListener(e -> loadProducts(txtSearch.getText().trim()));
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = selectedModelRow();
            if (r >= 0) {
                txtSku.setText((String) tableModel.getValueAt(r,1));
                txtName.setText((String) tableModel.getValueAt(r,2));
                txtPrice.setText(tableModel.getValueAt(r,3).toString());
                txtQty.setText(tableModel.getValueAt(r,4).toString());
            }
        });
    }

    private void loadProducts() { loadProducts(""); }

    private void loadProducts(String term) {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<List<Product>, Void>() {
            @Override protected List<Product> doInBackground() throws Exception {
                return service.search(term, 100, 0);
            }
            @Override protected void done() {
                try {
                    List<Product> list = get();
                    tableModel.setRowCount(0);
                    for (Product p : list) {
                        tableModel.addRow(new Object[]{p.getId(), p.getSku(), p.getName(), p.getPrice(), p.getQtyInStock()});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProductListPanel.this, "Failed to load products: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onAdd() {
        try {
            Product p = new Product();
            p.setSku(txtSku.getText().trim());
            p.setName(txtName.getText().trim());
            p.setPrice(new BigDecimal(txtPrice.getText().trim()));
            p.setQtyInStock(Integer.parseInt(txtQty.getText().trim()));
            p.setActive(true);
            service.createProduct(p);
            loadProducts();
            JOptionPane.showMessageDialog(this, "Product added");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        int r = selectedModelRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a product first"); return; }
        try {
            Product p = new Product();
            p.setId((int) tableModel.getValueAt(r,0));
            p.setSku(txtSku.getText().trim());
            p.setName(txtName.getText().trim());
            p.setPrice(new BigDecimal(txtPrice.getText().trim()));
            p.setQtyInStock(Integer.parseInt(txtQty.getText().trim()));
            p.setActive(true);
            service.updateProduct(p);
            loadProducts();
            JOptionPane.showMessageDialog(this, "Product updated");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int r = selectedModelRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a product first"); return; }
        int id = (int) tableModel.getValueAt(r,0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return new javaapplication1.erp.dao.impl.ProductDAOImpl().delete(id);
            }
            @Override protected void done() {
                try {
                    if (get()) { loadProducts(); JOptionPane.showMessageDialog(ProductListPanel.this, "Deleted"); }
                    else JOptionPane.showMessageDialog(ProductListPanel.this, "Delete failed");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProductListPanel.this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        loadProducts(txtSearch.getText().trim());
    }

    public void refreshData() {
        txtSearch.setText("");
        loadProducts("");
    }

    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }
}
