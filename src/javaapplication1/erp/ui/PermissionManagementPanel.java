package javaapplication1.erp.ui;

import javaapplication1.erp.service.RolePermissionService;
import javaapplication1.erp.util.Permission;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Admin panel to configure role permissions.
 */
public class PermissionManagementPanel extends JPanel {
    private final RolePermissionService service = new RolePermissionService();
    private final JComboBox<String> cmbRole = new JComboBox<>();
    private final Map<Permission, JCheckBox> checkboxes = new EnumMap<>(Permission.class);
    private final Map<String, Integer> roleIdByName = new java.util.HashMap<>();
    private final JButton btnSave = new JButton("Save Permissions");

    public PermissionManagementPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel();
        top.add(new JLabel("Role"));
        top.add(cmbRole);
        top.add(btnSave);
        add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new GridLayout(0, 2, 6, 6));
        for (Permission permission : Permission.values()) {
            JCheckBox cb = new JCheckBox(permission.name());
            checkboxes.put(permission, cb);
            center.add(cb);
        }
        add(center, BorderLayout.CENTER);
        bind();
        loadRolesAndPermissions();
    }

    private void bind() {
        cmbRole.addActionListener(e -> loadForSelectedRole());
        btnSave.addActionListener(e -> save());
    }

    private void loadRolesAndPermissions() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<RolePermissionService.RoleRow> roles;
            private Map<Integer, Set<Permission>> permissionMap;

            @Override
            protected Void doInBackground() throws Exception {
                roles = service.roles();
                permissionMap = service.permissionMap();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cmbRole.removeAllItems();
                    roleIdByName.clear();
                    for (RolePermissionService.RoleRow role : roles) {
                        cmbRole.addItem(role.name);
                        roleIdByName.put(role.name, role.id);
                    }
                    if (cmbRole.getItemCount() > 0) {
                        cmbRole.setSelectedIndex(0);
                        applyPermissions(permissionMap.getOrDefault(roleIdByName.get(cmbRole.getSelectedItem()), EnumSet.noneOf(Permission.class)));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PermissionManagementPanel.this, "Failed to load permissions: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadForSelectedRole() {
        String roleName = (String) cmbRole.getSelectedItem();
        if (roleName == null) {
            return;
        }
        Integer roleId = roleIdByName.get(roleName);
        if (roleId == null) {
            return;
        }
        SwingWorker<Map<Integer, Set<Permission>>, Void> worker = new SwingWorker<Map<Integer, Set<Permission>>, Void>() {
            @Override
            protected Map<Integer, Set<Permission>> doInBackground() throws Exception {
                return service.permissionMap();
            }

            @Override
            protected void done() {
                try {
                    Map<Integer, Set<Permission>> map = get();
                    applyPermissions(map.getOrDefault(roleId, EnumSet.noneOf(Permission.class)));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PermissionManagementPanel.this, "Failed to load role permissions: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyPermissions(Set<Permission> permissions) {
        for (Map.Entry<Permission, JCheckBox> entry : checkboxes.entrySet()) {
            entry.getValue().setSelected(permissions.contains(entry.getKey()));
        }
    }

    private void save() {
        String roleName = (String) cmbRole.getSelectedItem();
        if (roleName == null) {
            return;
        }
        Integer roleId = roleIdByName.get(roleName);
        if (roleId == null) {
            return;
        }
        Set<Permission> selected = EnumSet.noneOf(Permission.class);
        for (Map.Entry<Permission, JCheckBox> entry : checkboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.saveRolePermissions(roleId, selected);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(PermissionManagementPanel.this,
                            "Permissions saved. Re-login to apply menu changes.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PermissionManagementPanel.this, "Save failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        loadRolesAndPermissions();
    }
}
