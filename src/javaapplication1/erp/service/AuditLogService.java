package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Read operations for audit logs.
 */
public class AuditLogService {
    public static class AuditLogRow {
        public int id;
        public Integer userId;
        public String action;
        public String entityName;
        public Integer entityId;
        public String details;
        public java.sql.Timestamp createdAt;
    }

    public List<AuditLogRow> search(String keyword, int limit) throws Exception {
        List<AuditLogRow> rows = new ArrayList<>();
        String sql = "SELECT id, user_id, action, entity_name, entity_id, details, created_at " +
                "FROM audit_logs WHERE action LIKE ? OR entity_name LIKE ? OR details LIKE ? " +
                "ORDER BY id DESC LIMIT ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            String q = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLogRow row = new AuditLogRow();
                    row.id = rs.getInt("id");
                    int uid = rs.getInt("user_id");
                    row.userId = rs.wasNull() ? null : uid;
                    row.action = rs.getString("action");
                    row.entityName = rs.getString("entity_name");
                    int eid = rs.getInt("entity_id");
                    row.entityId = rs.wasNull() ? null : eid;
                    row.details = rs.getString("details");
                    row.createdAt = rs.getTimestamp("created_at");
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
