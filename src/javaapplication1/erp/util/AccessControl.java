package javaapplication1.erp.util;

import javaapplication1.erp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Set;

/**
 * Role-based permission resolver.
 */
public final class AccessControl {
    private AccessControl() {
    }

    public static boolean hasPermission(User user, Permission permission) {
        if (user == null) {
            return false;
        }
        return resolvePermissions(user.getRoleId()).contains(permission);
    }

    private static Set<Permission> resolvePermissions(int roleId) {
        Set<Permission> fromDb = loadPermissionsFromDb(roleId);
        if (!fromDb.isEmpty()) {
            return fromDb;
        }
        if (roleId == 1) {
            return EnumSet.allOf(Permission.class);
        }
        return EnumSet.of(
                Permission.MANAGE_PRODUCTS,
                Permission.MANAGE_CUSTOMERS,
                Permission.MANAGE_SUPPLIERS,
                Permission.MANAGE_INVENTORY,
                Permission.CREATE_SALES,
                Permission.VIEW_REPORTS
        );
    }

    private static Set<Permission> loadPermissionsFromDb(int roleId) {
        Set<Permission> permissions = new HashSet<>();
        String sql = "SELECT permission_key FROM role_permissions WHERE role_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("permission_key");
                    try {
                        permissions.add(Permission.valueOf(key));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore unknown keys.
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to hardcoded defaults when DB is unavailable.
        }
        return permissions;
    }
}
