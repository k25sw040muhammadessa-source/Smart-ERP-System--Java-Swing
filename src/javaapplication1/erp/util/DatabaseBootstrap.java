package javaapplication1.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ensures core schema exists and seeds default data directly in MySQL.
 */
public final class DatabaseBootstrap {
    private DatabaseBootstrap() {}

    public static void ensureSchemaAndSeed() throws SQLException {
        createDatabaseIfMissing();
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                createCoreTables(c);
                seedRoles(c);
                seedPermissions(c);
                seedAdminUser(c);
                seedProducts(c);
                seedUnits(c);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private static void createDatabaseIfMissing() throws SQLException {
        String host = DBConfig.get("db.host");
        String port = DBConfig.get("db.port");
        String user = DBConfig.get("db.user");
        String pass = DBConfig.get("db.password");
        String dbName = DBConfig.get("db.name");
        String params = DBConfig.get("db.params");
        if (params == null || params.isEmpty()) {
            params = "useSSL=false&serverTimezone=UTC";
        }

        String serverUrl = String.format("jdbc:mysql://%s:%s/?%s", host, port, params);
        try (Connection c = DriverManager.getConnection(serverUrl, user, pass);
             Statement st = c.createStatement()) {
            st.execute("CREATE DATABASE IF NOT EXISTS " + dbName + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        }
    }

    private static void createCoreTables(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS roles (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL UNIQUE" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS role_permissions (" +
                    "role_id INT NOT NULL," +
                    "permission_key VARCHAR(60) NOT NULL," +
                    "PRIMARY KEY (role_id, permission_key)," +
                    "CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "email VARCHAR(100) NOT NULL UNIQUE," +
                    "role_id INT NOT NULL," +
                    "active TINYINT NOT NULL DEFAULT 1," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sku VARCHAR(50) NOT NULL UNIQUE," +
                    "name VARCHAR(150) NOT NULL," +
                    "description TEXT," +
                    "price DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "cost DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "qty_in_stock INT NOT NULL DEFAULT 0," +
                    "reorder_level INT NOT NULL DEFAULT 5," +
                    "active TINYINT NOT NULL DEFAULT 1," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS units (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(80) NOT NULL UNIQUE," +
                    "symbol VARCHAR(20) NOT NULL UNIQUE," +
                    "active TINYINT NOT NULL DEFAULT 1," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(120) NOT NULL," +
                    "contact_name VARCHAR(120)," +
                    "phone VARCHAR(30)," +
                    "email VARCHAR(120)," +
                    "address VARCHAR(255)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(120) NOT NULL," +
                    "contact_name VARCHAR(120)," +
                    "phone VARCHAR(30)," +
                    "email VARCHAR(120)," +
                    "address VARCHAR(255)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "first_name VARCHAR(80) NOT NULL," +
                    "last_name VARCHAR(80) NOT NULL," +
                    "email VARCHAR(120) NOT NULL," +
                    "phone VARCHAR(30)," +
                    "hire_date DATE NOT NULL," +
                    "department VARCHAR(80)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS inventory_movements (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "qty INT NOT NULL," +
                    "type VARCHAR(10) NOT NULL," +
                    "ref_type VARCHAR(30)," +
                    "ref_id INT," +
                    "movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "note VARCHAR(255)," +
                    "CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS sales_orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "order_no VARCHAR(40) NOT NULL UNIQUE," +
                    "order_date DATE NOT NULL," +
                    "customer_id INT NOT NULL," +
                    "total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS sales_order_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sales_order_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "qty INT NOT NULL," +
                    "unit_price DECIMAL(12,2) NOT NULL," +
                    "line_total DECIMAL(12,2) NOT NULL," +
                    "CONSTRAINT fk_so_item_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_so_item_product FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS sales_invoices (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "invoice_no VARCHAR(40) NOT NULL UNIQUE," +
                    "sales_order_id INT NOT NULL UNIQUE," +
                    "invoice_date DATE NOT NULL," +
                    "amount_due DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID'," +
                    "CONSTRAINT fk_sales_invoice_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS purchases (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "purchase_no VARCHAR(40) NOT NULL UNIQUE," +
                    "supplier_id INT NOT NULL," +
                    "purchase_date DATE NOT NULL," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'," +
                    "total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS purchase_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "purchase_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "qty INT NOT NULL," +
                    "unit_cost DECIMAL(12,2) NOT NULL," +
                    "line_total DECIMAL(12,2) NOT NULL," +
                    "CONSTRAINT fk_purchase_item_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_purchase_item_product FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS purchase_invoices (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "invoice_no VARCHAR(40) NOT NULL UNIQUE," +
                    "purchase_id INT NOT NULL UNIQUE," +
                    "invoice_date DATE NOT NULL," +
                    "amount_due DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                    "payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID'," +
                    "CONSTRAINT fk_purchase_invoice_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NULL," +
                    "action VARCHAR(60) NOT NULL," +
                    "entity_name VARCHAR(60) NOT NULL," +
                    "entity_id INT NULL," +
                    "details VARCHAR(255)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");

            st.execute("CREATE INDEX idx_products_name ON products(name)");
        } catch (SQLException ex) {
            if (!"42000".equals(ex.getSQLState()) && ex.getErrorCode() != 1061) {
                throw ex;
            }
        }

        try (Statement st = c.createStatement()) {
            st.execute("CREATE INDEX idx_products_sku ON products(sku)");
        } catch (SQLException ex) {
            if (!"42000".equals(ex.getSQLState()) && ex.getErrorCode() != 1061) {
                throw ex;
            }
        }
        try (Statement st = c.createStatement()) {
            st.execute("CREATE INDEX idx_inventory_movement_date ON inventory_movements(movement_date)");
        } catch (SQLException ex) {
            if (!"42000".equals(ex.getSQLState()) && ex.getErrorCode() != 1061) {
                throw ex;
            }
        }
        ensureColumn(c, "sales_invoices", "paid_amount", "DECIMAL(12,2) NOT NULL DEFAULT 0.00");
        ensureColumn(c, "sales_invoices", "payment_status", "VARCHAR(20) NOT NULL DEFAULT 'UNPAID'");
        ensureColumn(c, "purchase_invoices", "paid_amount", "DECIMAL(12,2) NOT NULL DEFAULT 0.00");
        ensureColumn(c, "purchase_invoices", "payment_status", "VARCHAR(20) NOT NULL DEFAULT 'UNPAID'");
    }

    private static void ensureColumn(Connection c, String table, String column, String definition) throws SQLException {
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
        try (Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            if (ex.getErrorCode() != 1060) { // duplicate column
                throw ex;
            }
        }
    }

    private static void seedRoles(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO roles (name) VALUES (?), (?)")) {
            ps.setString(1, "Admin");
            ps.setString(2, "User");
            ps.executeUpdate();
        }
    }

    private static void seedPermissions(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id, name FROM roles");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int roleId = rs.getInt("id");
                String roleName = rs.getString("name");
                for (Permission permission : Permission.values()) {
                    if ("Admin".equalsIgnoreCase(roleName) || defaultUserPermission(permission)) {
                        upsertPermission(c, roleId, permission.name());
                    }
                }
            }
        }
    }

    private static boolean defaultUserPermission(Permission permission) {
        return permission == Permission.MANAGE_PRODUCTS
                || permission == Permission.MANAGE_CUSTOMERS
                || permission == Permission.MANAGE_SUPPLIERS
                || permission == Permission.MANAGE_INVENTORY
                || permission == Permission.CREATE_SALES
                || permission == Permission.VIEW_REPORTS;
    }

    private static void upsertPermission(Connection c, int roleId, String permissionKey) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT IGNORE INTO role_permissions (role_id, permission_key) VALUES (?, ?)")) {
            ps.setInt(1, roleId);
            ps.setString(2, permissionKey);
            ps.executeUpdate();
        }
    }

    private static void seedAdminUser(Connection c) throws SQLException {
        int adminRoleId = getRoleId(c, "Admin");
        String hash = PasswordUtil.hashPassword("admin123");

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users (username, password_hash, email, role_id, active) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), role_id = VALUES(role_id), active = VALUES(active), email = VALUES(email)")) {
            ps.setString(1, "admin");
            ps.setString(2, hash);
            ps.setString(3, "admin@erp.local");
            ps.setInt(4, adminRoleId);
            ps.setBoolean(5, true);
            ps.executeUpdate();
        }
    }

    private static int getRoleId(Connection c, String roleName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM roles WHERE name = ?")) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Role not found: " + roleName);
    }

    private static void seedProducts(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO products (sku, name, description, price, cost, qty_in_stock, reorder_level) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), " +
                        "price = VALUES(price), cost = VALUES(cost), qty_in_stock = VALUES(qty_in_stock), reorder_level = VALUES(reorder_level)")) {
            ps.setString(1, "P-1000");
            ps.setString(2, "Widget A");
            ps.setString(3, "Standard widget");
            ps.setBigDecimal(4, new java.math.BigDecimal("19.99"));
            ps.setBigDecimal(5, new java.math.BigDecimal("10.00"));
            ps.setInt(6, 100);
            ps.setInt(7, 10);
            ps.executeUpdate();

            ps.setString(1, "P-1001");
            ps.setString(2, "Widget B");
            ps.setString(3, "Advanced widget");
            ps.setBigDecimal(4, new java.math.BigDecimal("29.99"));
            ps.setBigDecimal(5, new java.math.BigDecimal("15.00"));
            ps.setInt(6, 50);
            ps.setInt(7, 5);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT IGNORE INTO customers (id, name, contact_name, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "ABC Traders");
            ps.setString(3, "Ali Raza");
            ps.setString(4, "+923001111111");
            ps.setString(5, "abc@client.com");
            ps.setString(6, "Lahore");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT IGNORE INTO suppliers (id, name, contact_name, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "Prime Supplies");
            ps.setString(3, "Hamza Khan");
            ps.setString(4, "+923003333333");
            ps.setString(5, "prime@supplier.com");
            ps.setString(6, "Islamabad");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT IGNORE INTO employees (id, first_name, last_name, email, phone, hire_date, department) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "Sara");
            ps.setString(3, "Ahmed");
            ps.setString(4, "sara@erp.local");
            ps.setString(5, "+923005555555");
            ps.setDate(6, java.sql.Date.valueOf("2025-01-01"));
            ps.setString(7, "Sales");
            ps.executeUpdate();
        }
    }

    private static void seedUnits(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT IGNORE INTO units (name, symbol, active) VALUES (?, ?, ?)")) {
            ps.setString(1, "Piece");
            ps.setString(2, "PCS");
            ps.setBoolean(3, true);
            ps.executeUpdate();

            ps.setString(1, "Kilogram");
            ps.setString(2, "KG");
            ps.setBoolean(3, true);
            ps.executeUpdate();

            ps.setString(1, "Liter");
            ps.setString(2, "LTR");
            ps.setBoolean(3, true);
            ps.executeUpdate();
        }
    }
}
