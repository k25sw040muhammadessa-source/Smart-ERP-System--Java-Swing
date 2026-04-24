package javaapplication1.erp.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple CLI check to validate DB connection and seeded data.
 */
public final class DatabaseConnectionCheck {
    private DatabaseConnectionCheck() {}

    public static void main(String[] args) {
        try {
            DatabaseBootstrap.ensureSchemaAndSeed();
            try (Connection c = DBUtil.getConnection();
                 Statement st = c.createStatement()) {
                int users = count(st, "SELECT COUNT(*) FROM users");
                int products = count(st, "SELECT COUNT(*) FROM products");
                int adminUsers = count(st, "SELECT COUNT(*) FROM users WHERE username='admin' AND active=1");
                System.out.println("DB_CONNECTION_OK");
                System.out.println("users=" + users);
                System.out.println("products=" + products);
                System.out.println("admin_active=" + adminUsers);
                System.out.println("default_login=admin/admin123");
            }
        } catch (Exception e) {
            System.err.println("DB_CONNECTION_FAILED: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int count(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
