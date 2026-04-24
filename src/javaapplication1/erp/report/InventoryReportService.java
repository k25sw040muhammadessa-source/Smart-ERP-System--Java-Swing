package javaapplication1.erp.report;

import javaapplication1.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds report data and exports low-stock reports.
 */
public class InventoryReportService {
    private static final String LOW_STOCK_SQL =
            "SELECT sku, name, reorder_level, " +
            "COALESCE(SUM(CASE WHEN type='IN' THEN qty WHEN type='OUT' THEN -qty ELSE qty END), 0) AS stock " +
            "FROM products p LEFT JOIN inventory_movements m ON p.id = m.product_id " +
            "WHERE p.active = 1 GROUP BY p.id, p.sku, p.name, p.reorder_level HAVING stock <= reorder_level ORDER BY stock ASC";

    public List<String[]> lowStockRows() throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOW_STOCK_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("sku"),
                        rs.getString("name"),
                        String.valueOf(rs.getInt("stock")),
                        String.valueOf(rs.getInt("reorder_level"))
                });
            }
        }
        return rows;
    }

    public void exportLowStockPdf(String outputPath) throws Exception {
        String[] headers = {"SKU", "Product", "Current Stock", "Reorder Level"};
        PdfReportUtil.generateTableReport(outputPath, "Low Stock Inventory Report", headers, lowStockRows());
    }
}
