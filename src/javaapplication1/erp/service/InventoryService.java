package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Inventory transaction service for stock in/out adjustments.
 */
public class InventoryService {
    private static final String SELECT_PRODUCT_SQL = "SELECT qty_in_stock FROM products WHERE id = ?";
    private static final String UPDATE_PRODUCT_STOCK_SQL = "UPDATE products SET qty_in_stock = ? WHERE id = ?";
    private static final String INSERT_MOVEMENT_SQL =
            "INSERT INTO inventory_movements (product_id, qty, type, ref_type, ref_id, note) VALUES (?,?,?,?,?,?)";

    private final AuditService auditService = new AuditService();

    public void stockIn(int productId, int qty, String note) throws Exception {
        adjust(productId, qty, "IN", "MANUAL", null, note);
    }

    public void stockOut(int productId, int qty, String note) throws Exception {
        adjust(productId, qty, "OUT", "MANUAL", null, note);
    }

    public void adjust(int productId, int qty, String type, String refType, Integer refId, String note) throws Exception {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be valid");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        ValidationUtil.requireNonEmpty(type, "Movement type");
        if (!"IN".equals(type) && !"OUT".equals(type)) {
            throw new IllegalArgumentException("Movement type must be IN or OUT");
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int currentStock;
                try (PreparedStatement ps = connection.prepareStatement(SELECT_PRODUCT_SQL)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Product not found: " + productId);
                        }
                        currentStock = rs.getInt("qty_in_stock");
                    }
                }

                int nextStock = "IN".equals(type) ? currentStock + qty : currentStock - qty;
                if (nextStock < 0) {
                    throw new IllegalArgumentException("Insufficient stock. Current stock: " + currentStock);
                }

                try (PreparedStatement ps = connection.prepareStatement(UPDATE_PRODUCT_STOCK_SQL)) {
                    ps.setInt(1, nextStock);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(INSERT_MOVEMENT_SQL)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, qty);
                    ps.setString(3, type);
                    ps.setString(4, refType);
                    if (refId == null) {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(5, refId);
                    }
                    ps.setString(6, note);
                    ps.executeUpdate();
                }

                connection.commit();
                auditService.log("STOCK_" + type, "inventory_movements", productId,
                        "qty=" + qty + ", refType=" + refType + ", refId=" + refId);
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
