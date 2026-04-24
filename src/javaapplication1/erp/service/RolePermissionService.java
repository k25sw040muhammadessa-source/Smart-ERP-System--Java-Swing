package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.Permission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DB-backed role permission administration.
 */
public class RolePermissionService {
    public static class RoleRow {
        public int id;
        public String name;
    }

    public List<RoleRow> roles() throws Exception {
        List<RoleRow> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM roles ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoleRow row = new RoleRow();
                row.id = rs.getInt("id");
                row.name = rs.getString("name");
                list.add(row);
            }
        }
        return list;
    }

    public Map<Integer, Set<Permission>> permissionMap() throws Exception {
        Map<Integer, Set<Permission>> map = new HashMap<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT role_id, permission_key FROM role_permissions");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int roleId = rs.getInt("role_id");
                String key = rs.getString("permission_key");
                try {
                    map.computeIfAbsent(roleId, x -> EnumSet.noneOf(Permission.class)).add(Permission.valueOf(key));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return map;
    }

    public void saveRolePermissions(int roleId, Set<Permission> permissions) throws Exception {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement del = c.prepareStatement("DELETE FROM role_permissions WHERE role_id = ?")) {
                    del.setInt(1, roleId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO role_permissions (role_id, permission_key) VALUES (?, ?)")) {
                    for (Permission permission : permissions) {
                        ins.setInt(1, roleId);
                        ins.setString(2, permission.name());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
