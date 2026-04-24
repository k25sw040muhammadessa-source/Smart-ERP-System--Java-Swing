package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Creates a purchase receipt and applies stock-in.
 */
public class PurchaseService {
    private final AuditService auditService = new AuditService();

    public static class LineItem {
        private final int productId;
        private final int qty;
        private final BigDecimal unitCost;

        public LineItem(int productId, int qty, BigDecimal unitCost) {
            this.productId = productId;
            this.qty = qty;
            this.unitCost = unitCost;
        }
    }

    public int createSingleItemPurchase(int supplierId, int productId, int qty, BigDecimal unitCost) throws Exception {
        return createPurchase(supplierId, java.util.Collections.singletonList(new LineItem(productId, qty, unitCost)));
    }

    public int createPurchase(int supplierId, List<LineItem> items) throws Exception {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("Supplier is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one line item is required");
        }
        String purchaseNo = "PO-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + System.currentTimeMillis() % 100000;
        BigDecimal grandTotal = BigDecimal.ZERO;
        int purchaseId;

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (LineItem item : items) {
                    validateLine(item);
                    grandTotal = grandTotal.add(item.unitCost.multiply(BigDecimal.valueOf(item.qty)));
                }
                purchaseId = insertPurchase(connection, purchaseNo, supplierId, grandTotal);
                for (LineItem item : items) {
                    BigDecimal lineTotal = item.unitCost.multiply(BigDecimal.valueOf(item.qty));
                    insertPurchaseItem(connection, purchaseId, item.productId, item.qty, item.unitCost, lineTotal);
                    incrementStock(connection, item.productId, item.qty);
                    insertInventoryMovement(connection, item.productId, item.qty, "IN", "PURCHASE", purchaseId,
                            "Auto stock-in for purchase " + purchaseNo);
                }
                upsertPurchaseInvoice(connection, purchaseNo, purchaseId, grandTotal);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }

        auditService.log("CREATE_PURCHASE", "purchases", purchaseId, "purchaseNo=" + purchaseNo + ", total=" + grandTotal);
        return purchaseId;
    }

    public void cancelPurchase(int purchaseId) throws Exception {
        String purchaseSql = "SELECT status, purchase_no FROM purchases WHERE id = ?";
        String itemSql = "SELECT product_id, qty FROM purchase_items WHERE purchase_id = ?";
        String updatePurchaseSql = "UPDATE purchases SET status='CANCELLED' WHERE id = ?";
        String updateInvoiceSql = "UPDATE purchase_invoices SET payment_status='CANCELLED' WHERE purchase_id = ?";
        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String status;
                String purchaseNo;
                try (PreparedStatement ps = connection.prepareStatement(purchaseSql)) {
                    ps.setInt(1, purchaseId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Purchase not found");
                        status = rs.getString("status");
                        purchaseNo = rs.getString("purchase_no");
                    }
                }
                if ("CANCELLED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("Purchase is already cancelled");
                }
                try (PreparedStatement ps = connection.prepareStatement(itemSql)) {
                    ps.setInt(1, purchaseId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int productId = rs.getInt("product_id");
                            int qty = rs.getInt("qty");
                            int stock = currentStock(connection, productId);
                            if (stock < qty) {
                                throw new IllegalArgumentException("Cannot cancel purchase. Product " + productId
                                        + " has insufficient stock for rollback.");
                            }
                            decrementStock(connection, productId, qty);
                            insertInventoryMovement(connection, productId, qty, "OUT", "PURCHASE_CANCEL", purchaseId,
                                    "Rollback for cancelled purchase " + purchaseNo);
                        }
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(updatePurchaseSql)) {
                    ps.setInt(1, purchaseId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(updateInvoiceSql)) {
                    ps.setInt(1, purchaseId);
                    ps.executeUpdate();
                }
                connection.commit();
                auditService.log("CANCEL_PURCHASE", "purchases", purchaseId, "status=CANCELLED");
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void validateLine(LineItem item) {
        if (item.productId <= 0 || item.qty <= 0) {
            throw new IllegalArgumentException("Product and quantity must be valid");
        }
        if (item.unitCost == null || item.unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit cost must be positive");
        }
    }

    private int insertPurchase(Connection connection, String purchaseNo, int supplierId, BigDecimal total) throws Exception {
        String sql = "INSERT INTO purchases (purchase_no, supplier_id, purchase_date, status, total_amount) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, purchaseNo);
            ps.setInt(2, supplierId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, "RECEIVED");
            ps.setBigDecimal(5, total);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new IllegalStateException("Failed to create purchase");
            }
        }
    }

    private void insertPurchaseItem(Connection connection, int purchaseId, int productId, int qty,
                                    BigDecimal unitCost, BigDecimal lineTotal) throws Exception {
        String sql = "INSERT INTO purchase_items (purchase_id, product_id, qty, unit_cost, line_total) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, purchaseId);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitCost);
            ps.setBigDecimal(5, lineTotal);
            ps.executeUpdate();
        }
    }

    private void upsertPurchaseInvoice(Connection connection, String purchaseNo, int purchaseId, BigDecimal amountDue) throws Exception {
        String invoiceNo = purchaseNo.replace("PO-", "PINV-");
        String sql = "INSERT INTO purchase_invoices (invoice_no, purchase_id, invoice_date, amount_due) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            ps.setInt(2, purchaseId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setBigDecimal(4, amountDue);
            ps.executeUpdate();
        }
    }

    private void incrementStock(Connection connection, int productId, int qty) throws Exception {
        String sql = "UPDATE products SET qty_in_stock = qty_in_stock + ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Product not found: " + productId);
            }
        }
    }

    private void decrementStock(Connection connection, int productId, int qty) throws Exception {
        String sql = "UPDATE products SET qty_in_stock = qty_in_stock - ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Product not found: " + productId);
            }
        }
    }

    private int currentStock(Connection connection, int productId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT qty_in_stock FROM products WHERE id=?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Product not found: " + productId);
                }
                return rs.getInt(1);
            }
        }
    }

    private void insertInventoryMovement(Connection connection, int productId, int qty, String type,
                                         String refType, Integer refId, String note) throws Exception {
        String sql = "INSERT INTO inventory_movements (product_id, qty, type, ref_type, ref_id, note) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    }
}
