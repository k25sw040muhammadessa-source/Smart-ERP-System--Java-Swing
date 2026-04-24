package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides lightweight KPI data for dashboard.
 */
public class DashboardService {
    public Map<String, String> fetchKpis() {
        Map<String, String> kpis = new HashMap<>();
        kpis.put("products", "0");
        kpis.put("customers", "0");
        kpis.put("suppliers", "0");
        kpis.put("todaySales", "0.00");
        kpis.put("lowStockCount", "0");

        try (Connection connection = DBUtil.getConnection()) {
            kpis.put("products", String.valueOf(count(connection, "SELECT COUNT(*) FROM products WHERE active = 1")));
            kpis.put("customers", String.valueOf(count(connection, "SELECT COUNT(*) FROM customers")));
            kpis.put("suppliers", String.valueOf(count(connection, "SELECT COUNT(*) FROM suppliers")));
            kpis.put("todaySales", sumTodaySales(connection).toPlainString());
            kpis.put("lowStockCount", String.valueOf(count(connection,
                    "SELECT COUNT(*) FROM products WHERE active = 1 AND qty_in_stock <= reorder_level")));
        } catch (Exception ignored) {
            // Keep defaults if tables are not initialized yet.
        }
        return kpis;
    }

    private int count(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private BigDecimal sumTodaySales(Connection connection) throws Exception {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales_orders WHERE order_date = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }
}
