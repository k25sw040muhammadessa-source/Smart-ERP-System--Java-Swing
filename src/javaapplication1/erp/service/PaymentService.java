package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles payment posting for invoices.
 */
public class PaymentService {
    public static class InvoiceRow {
        public int id;
        public String invoiceNo;
        public Integer refId;
        public BigDecimal amountDue;
        public BigDecimal paidAmount;
        public String paymentStatus;
        public java.sql.Date invoiceDate;
    }

    private final AuditService auditService = new AuditService();

    public List<InvoiceRow> salesInvoices() throws Exception {
        return fetch("SELECT id, invoice_no, sales_order_id ref_id, amount_due, paid_amount, payment_status, invoice_date " +
                "FROM sales_invoices ORDER BY id DESC LIMIT 500");
    }

    public List<InvoiceRow> purchaseInvoices() throws Exception {
        return fetch("SELECT id, invoice_no, purchase_id ref_id, amount_due, paid_amount, payment_status, invoice_date " +
                "FROM purchase_invoices ORDER BY id DESC LIMIT 500");
    }

    public void postSalesPayment(int invoiceId, BigDecimal amount) throws Exception {
        postPayment("sales_invoices", "sales_order_id", "sales", invoiceId, amount);
    }

    public void postPurchasePayment(int invoiceId, BigDecimal amount) throws Exception {
        postPayment("purchase_invoices", "purchase_id", "purchase", invoiceId, amount);
    }

    private List<InvoiceRow> fetch(String sql) throws Exception {
        List<InvoiceRow> rows = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InvoiceRow row = new InvoiceRow();
                row.id = rs.getInt("id");
                row.invoiceNo = rs.getString("invoice_no");
                int ref = rs.getInt("ref_id");
                row.refId = rs.wasNull() ? null : ref;
                row.amountDue = rs.getBigDecimal("amount_due");
                row.paidAmount = rs.getBigDecimal("paid_amount");
                row.paymentStatus = rs.getString("payment_status");
                row.invoiceDate = rs.getDate("invoice_date");
                rows.add(row);
            }
        }
        return rows;
    }

    private void postPayment(String table, String refCol, String entity, int invoiceId, BigDecimal amount) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        String selectSql = "SELECT " + refCol + ", amount_due, paid_amount, payment_status FROM " + table + " WHERE id = ?";
        String updateSql = "UPDATE " + table + " SET paid_amount = ?, payment_status = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(selectSql)) {
                ps.setInt(1, invoiceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Invoice not found: " + invoiceId);
                    }
                    int refId = rs.getInt(1);
                    BigDecimal due = rs.getBigDecimal("amount_due");
                    BigDecimal paid = rs.getBigDecimal("paid_amount");
                    String status = rs.getString("payment_status");
                    if ("CANCELLED".equalsIgnoreCase(status)) {
                        throw new IllegalArgumentException("Cannot post payment on cancelled invoice");
                    }
                    BigDecimal nextPaid = paid.add(amount);
                    if (nextPaid.compareTo(due) > 0) {
                        throw new IllegalArgumentException("Payment exceeds amount due");
                    }
                    String nextStatus = nextPaid.compareTo(BigDecimal.ZERO) == 0 ? "UNPAID"
                            : nextPaid.compareTo(due) < 0 ? "PARTIAL" : "PAID";
                    try (PreparedStatement up = c.prepareStatement(updateSql)) {
                        up.setBigDecimal(1, nextPaid);
                        up.setString(2, nextStatus);
                        up.setInt(3, invoiceId);
                        up.executeUpdate();
                    }
                    c.commit();
                    auditService.log("POST_PAYMENT", table, invoiceId,
                            "entity=" + entity + ", refId=" + refId + ", amount=" + amount + ", status=" + nextStatus);
                }
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
