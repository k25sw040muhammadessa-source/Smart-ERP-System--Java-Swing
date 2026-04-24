package javaapplication1.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBUtil: provides JDBC connections using DriverManager.
 */
public final class DBUtil {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        String url = DBConfig.getUrl();
        String user = DBConfig.get("db.user");
        String pass = DBConfig.get("db.password");
        return DriverManager.getConnection(url, user, pass);
    }
}
