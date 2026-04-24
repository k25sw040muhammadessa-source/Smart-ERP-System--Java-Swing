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
 * Sales order creation with invoice generation and stock deduction.
 */
public class SalesService {
    private final AuditService auditService = new AuditService();

    public static class LineItem {
        private final int productId;
        private final int qty;
        private final BigDecimal unitPrice;

        public LineItem(int productId, int qty, BigDecimal unitPrice) {
            this.productId = productId;
            this.qty = qty;
            this.unitPrice = unitPrice;
        }
    }

    public int createSingleItemSale(int customerId, int productId, int qty, BigDecimal unitPrice) throws Exception {
        return createSale(customerId, java.util.Collections.singletonList(new LineItem(productId, qty, unitPrice)));
    }

    public int createSale(int customerId, List<LineItem> items) throws Exception {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one line item is required");
        }

        String orderNo = "SO-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + System.currentTimeMillis() % 100000;
        BigDecimal grandTotal = BigDecimal.ZERO;
        int orderId;

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (LineItem item : items) {
                    validateLine(item);
                    int stock = currentStock(connection, item.productId);
                    if (stock < item.qty) {
                        throw new IllegalArgumentException("Insufficient stock for product " + item.productId + ". Available: " + stock);
                    }
                }

                for (LineItem item : items) {
                    grandTotal = grandTotal.add(item.unitPrice.multiply(BigDecimal.valueOf(item.qty)));
                }
                orderId = insertSalesOrder(connection, orderNo, customerId, grandTotal);
                for (LineItem item : items) {
                    BigDecimal lineTotal = item.unitPrice.multiply(BigDecimal.valueOf(item.qty));
                    insertSalesItem(connection, orderId, item.productId, item.qty, item.unitPrice, lineTotal);
                    int stock = currentStock(connection, item.productId);
                    updateStock(connection, item.productId, stock - item.qty);
                    insertInventoryMovement(connection, item.productId, item.qty, "OUT", "SALES", orderId,
                            "Auto deduction for order " + orderNo);
                }
                insertInvoice(connection, orderNo, orderId, grandTotal);

                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }

        auditService.log("CREATE_SALE", "sales_orders", orderId, "orderNo=" + orderNo + ", total=" + grandTotal);
        return orderId;
    }

    public void cancelSale(int salesOrderId) throws Exception {
        String orderSql = "SELECT status, order_no FROM sales_orders WHERE id = ?";
        String itemSql = "SELECT product_id, qty FROM sales_order_items WHERE sales_order_id = ?";
        String updateOrderSql = "UPDATE sales_orders SET status='CANCELLED' WHERE id = ?";
        String updateInvoiceSql = "UPDATE sales_invoices SET payment_status='CANCELLED' WHERE sales_order_id = ?";
        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String status;
                String orderNo;
                try (PreparedStatement ps = connection.prepareStatement(orderSql)) {
                    ps.setInt(1, salesOrderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Sales order not found");
                        status = rs.getString("status");
                        orderNo = rs.getString("order_no");
                    }
                }
                if ("CANCELLED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("Sales order is already cancelled");
                }
                try (PreparedStatement ps = connection.prepareStatement(itemSql);
                     PreparedStatement stockUp = connection.prepareStatement("UPDATE products SET qty_in_stock = qty_in_stock + ? WHERE id = ?")) {
                    ps.setInt(1, salesOrderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int productId = rs.getInt("product_id");
                            int qty = rs.getInt("qty");
                            stockUp.setInt(1, qty);
                            stockUp.setInt(2, productId);
                            stockUp.addBatch();
                            insertInventoryMovement(connection, productId, qty, "IN", "SALES_CANCEL", salesOrderId,
                                    "Rollback for cancelled order " + orderNo);
                        }
                        stockUp.executeBatch();
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                    ps.setInt(1, salesOrderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(updateInvoiceSql)) {
                    ps.setInt(1, salesOrderId);
                    ps.executeUpdate();
                }
                connection.commit();
                auditService.log("CANCEL_SALE", "sales_orders", salesOrderId, "status=CANCELLED");
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
        if (item.unitPrice == null || item.unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
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

    private int insertSalesOrder(Connection connection, String orderNo, int customerId, BigDecimal total) throws Exception {
        String sql = "INSERT INTO sales_orders (order_no, customer_id, order_date, status, total_amount) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, orderNo);
            ps.setInt(2, customerId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, "CONFIRMED");
            ps.setBigDecimal(5, total);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new IllegalStateException("Failed to create sales order");
            }
        }
    }

    private void insertSalesItem(Connection connection, int orderId, int productId, int qty,
                                 BigDecimal unitPrice, BigDecimal lineTotal) throws Exception {
        String sql = "INSERT INTO sales_order_items (sales_order_id, product_id, qty, unit_price, line_total) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitPrice);
            ps.setBigDecimal(5, lineTotal);
            ps.executeUpdate();
        }
    }

    private void insertInvoice(Connection connection, String orderNo, int orderId, BigDecimal amountDue) throws Exception {
        String invoiceNo = orderNo.replace("SO-", "INV-");
        String sql = "INSERT INTO sales_invoices (invoice_no, sales_order_id, invoice_date, amount_due) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            ps.setInt(2, orderId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setBigDecimal(4, amountDue);
            ps.executeUpdate();
        }
    }

    private void updateStock(Connection connection, int productId, int newStock) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE products SET qty_in_stock=? WHERE id=?")) {
            ps.setInt(1, newStock);
            ps.setInt(2, productId);
            ps.executeUpdate();
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
