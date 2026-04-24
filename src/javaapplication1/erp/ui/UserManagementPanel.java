package javaapplication1.erp.ui;

import javaapplication1.erp.dao.impl.UserDAOImpl;
import javaapplication1.erp.model.User;
import javaapplication1.erp.service.UserService;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.UIUtils;
import javaapplication1.erp.ui.theme.AnimatedPanel;
import javaapplication1.erp.ui.theme.PlaceholderTextField;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin user management with role assignment and disable/reset.
 */
public class UserManagementPanel extends JPanel {
    private final UserService service = new UserService(new UserDAOImpl());
    // roleNameToId: populated from DB at startup - key = display name, value = role_id
    private final Map<String, Integer> roleNameToId = new LinkedHashMap<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Email", "Role", "Active"}, 0);
    private final JTable table = new JTable(tableModel);

    private final JTextField txtUsername = new PlaceholderTextField("e.g. john_doe", 20);
    private final JTextField txtEmail = new PlaceholderTextField("e.g. john@example.com", 20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Admin", "User"});
    private final JCheckBox chkActive = new JCheckBox("Active", true);
    private final JTextField txtSearch = new JTextField(20);

    private final JButton btnSearch = new JButton("Search");
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnResetPassword = new JButton("Reset Password");

    public UserManagementPanel() {
        setLayout(new BorderLayout(8,8));
        setBackground(Theme.BACKGROUND);

        // Load roles from DB first
        loadRolesFromDB();
        // Populate combo from DB roles
        cmbRole.removeAllItems();
        if (roleNameToId.isEmpty()) {
            cmbRole.addItem("Admin");
            cmbRole.addItem("User");
        } else {
            roleNameToId.keySet().forEach(cmbRole::addItem);
        }
        
        // Create animated form container
        JPanel formContent = buildForm();
        AnimatedPanel animatedForm = new AnimatedPanel(new BorderLayout());
        animatedForm.setPreferredSize(new Dimension(460, 520));
        animatedForm.setBorder(new EmptyBorder(8, 8, 8, 8));
        animatedForm.add(formContent, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animatedForm, buildTablePanel());
        split.setDividerLocation(460);
        add(split, BorderLayout.CENTER);
        
        // Apply styling
        UIUtils.styleTable(table);
        UIUtils.styleButton(btnSearch);
        UIUtils.styleButton(btnAdd);
        UIUtils.styleButton(btnUpdate);
        UIUtils.styleButton(btnDelete);
        UIUtils.styleButton(btnResetPassword);
        UIUtils.styleTextField(txtUsername);
        UIUtils.styleTextField(txtEmail);
        UIUtils.styleTextField(txtPassword);
        UIUtils.styleComboBox(cmbRole);
        UIUtils.styleTextField(txtSearch);
        
        bind();
        loadUsers("");
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
        cardPanel.setPreferredSize(new Dimension(410, 490));

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(Theme.FONT_SUBHEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        cardPanel.add(createFieldBlock("Username", txtUsername));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Email", txtEmail));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Password (required for add/reset)", txtPassword));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Role", cmbRole));
        cardPanel.add(Box.createVerticalStrut(12));
        
        chkActive.setFont(Theme.FONT_BODY);
        chkActive.setForeground(Theme.TEXT_PRIMARY);
        chkActive.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(chkActive);
        cardPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(320, 90));
        btnAdd.setFont(Theme.FONT_BODY_MEDIUM);
        btnAdd.setPreferredSize(new Dimension(90, 38));
        btnUpdate.setFont(Theme.FONT_BODY_MEDIUM);
        btnUpdate.setPreferredSize(new Dimension(90, 38));
        btnDelete.setFont(Theme.FONT_BODY_MEDIUM);
        btnDelete.setPreferredSize(new Dimension(90, 38));
        btnResetPassword.setFont(Theme.FONT_BODY_MEDIUM);
        btnResetPassword.setPreferredSize(new Dimension(120, 38));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnResetPassword);
        cardPanel.add(buttonPanel);

        wrapper.add(cardPanel, new GridBagConstraints());
        return wrapper;
    }

    private JPanel createFieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(Component.CENTER_ALIGNMENT);
        block.setMaximumSize(new Dimension(330, 62));

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY_MEDIUM);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof JTextField) {
            ((JTextField) field).setFont(Theme.FONT_BODY);
            ((JTextField) field).setPreferredSize(new Dimension(320, 36));
            ((JTextField) field).setMaximumSize(new Dimension(320, 36));
        } else if (field instanceof JPasswordField) {
            ((JPasswordField) field).setFont(Theme.FONT_BODY);
            ((JPasswordField) field).setPreferredSize(new Dimension(320, 36));
            ((JPasswordField) field).setMaximumSize(new Dimension(320, 36));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setPreferredSize(new Dimension(320, 36));
            ((JComboBox<?>) field).setMaximumSize(new Dimension(320, 36));
        }
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        top.add(txtSearch);
        top.add(btnSearch);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void bind() {
        btnSearch.addActionListener(e -> loadUsers(txtSearch.getText().trim()));
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnResetPassword.addActionListener(e -> onResetPassword());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = selectedModelRow();
            if (row < 0) {
                return;
            }
            txtUsername.setText(String.valueOf(tableModel.getValueAt(row, 1)));
            txtEmail.setText(String.valueOf(tableModel.getValueAt(row, 2)));
            String roleDisplay = String.valueOf(tableModel.getValueAt(row, 3));
            cmbRole.setSelectedItem(roleDisplay);
            chkActive.setSelected(Boolean.parseBoolean(String.valueOf(tableModel.getValueAt(row, 4))));
            txtPassword.setText("");
        });
    }

    private void loadUsers(String term) {
        // Refresh roles map before loading users
        loadRolesFromDB();
        // Build reverse map: id -> name
        Map<Integer, String> idToRoleName = new java.util.HashMap<>();
        roleNameToId.forEach((name, id) -> idToRoleName.put(id, name));

        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return service.search(term);
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User user : users) {
                        String roleName = idToRoleName.getOrDefault(user.getRoleId(),
                                user.getRoleId() == 1 ? "Admin" : "User");
                        tableModel.addRow(new Object[]{
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                roleName,
                                user.isActive()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "Failed to load users: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private User readForm() {
        User user = new User();
        user.setUsername(txtUsername.getText().trim());
        user.setEmail(txtEmail.getText().trim());
        String selectedRole = (String) cmbRole.getSelectedItem();
        int roleId = roleNameToId.getOrDefault(selectedRole,
                "Admin".equalsIgnoreCase(selectedRole) ? 1 : 2);
        user.setRoleId(roleId);
        user.setActive(chkActive.isSelected());
        return user;
    }

    /** Load roles table from DB into roleNameToId map */
    private void loadRolesFromDB() {
        roleNameToId.clear();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM roles ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roleNameToId.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (Exception ignored) {}
    }

    private int selectedUserId() {
        int row = selectedModelRow();
        if (row < 0) {
            return -1;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    private void onAdd() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.create(readForm(), new String(txtPassword.getPassword()));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    txtPassword.setText("");
                    loadUsers("");
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "User created");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "Create failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onUpdate() {
        int userId = selectedUserId();
        if (userId < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first");
            return;
        }
        if (userId == UserSession.getInstance().getUser().getId() && !chkActive.isSelected()) {
            JOptionPane.showMessageDialog(this, "You cannot disable your own account");
            return;
        }
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                User user = readForm();
                user.setId(userId);
                return service.update(user);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadUsers("");
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "User updated");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "Update failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onResetPassword() {
        int userId = selectedUserId();
        if (userId < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first");
            return;
        }
        String password = new String(txtPassword.getPassword());
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return service.resetPassword(userId, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        txtPassword.setText("");
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "Password reset successful");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "Reset failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onDelete() {
        int userId = selectedUserId();
        if (userId < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first");
            return;
        }
        if (userId == UserSession.getInstance().getUser().getId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this user?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return service.delete(userId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        txtUsername.setText("");
                        txtEmail.setText("");
                        txtPassword.setText("");
                        cmbRole.setSelectedIndex(0);
                        chkActive.setSelected(true);
                        loadUsers("");
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "User deleted");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "Delete failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        loadUsers(txtSearch.getText().trim());
    }

    public void refreshData() {
        txtSearch.setText("");
        loadUsers("");
    }
}
