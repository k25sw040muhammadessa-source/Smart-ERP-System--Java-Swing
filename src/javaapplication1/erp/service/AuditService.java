package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Records audit events for critical actions.
 */
public class AuditService {
    private static final String INSERT_SQL =
            "INSERT INTO audit_logs (user_id, action, entity_name, entity_id, details) VALUES (?,?,?,?,?)";

    public void log(String action, String entityName, Integer entityId, String details) {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            Integer userId = UserSession.getInstance().getUser() == null ? null : UserSession.getInstance().getUser().getId();
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, entityName);
            if (entityId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, entityId);
            }
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception ignored) {
            // Never block business flow because of audit failure.
        }
    }
}
