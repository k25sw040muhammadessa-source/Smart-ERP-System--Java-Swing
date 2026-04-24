package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer/supplier ledger summaries.
 */
public class LedgerService {
    public static class LedgerRow {
        public int partyId;
        public String partyName;
        public java.math.BigDecimal totalInvoiced;
        public java.math.BigDecimal totalPaid;
        public java.math.BigDecimal balance;
    }

    public List<LedgerRow> customerLedger() throws Exception {
        String sql = "SELECT c.id party_id, c.name party_name, " +
                "COALESCE(SUM(so.total_amount),0) total_invoiced, " +
                "COALESCE(SUM(si.paid_amount),0) total_paid " +
                "FROM customers c " +
                "LEFT JOIN sales_orders so ON so.customer_id = c.id " +
                "LEFT JOIN sales_invoices si ON si.sales_order_id = so.id " +
                "GROUP BY c.id, c.name ORDER BY c.name";
        return fetch(sql);
    }

    public List<LedgerRow> supplierLedger() throws Exception {
        String sql = "SELECT s.id party_id, s.name party_name, " +
                "COALESCE(SUM(p.total_amount),0) total_invoiced, " +
                "COALESCE(SUM(pi.paid_amount),0) total_paid " +
                "FROM suppliers s " +
                "LEFT JOIN purchases p ON p.supplier_id = s.id " +
                "LEFT JOIN purchase_invoices pi ON pi.purchase_id = p.id " +
                "GROUP BY s.id, s.name ORDER BY s.name";
        return fetch(sql);
    }

    private List<LedgerRow> fetch(String sql) throws Exception {
        List<LedgerRow> rows = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LedgerRow row = new LedgerRow();
                row.partyId = rs.getInt("party_id");
                row.partyName = rs.getString("party_name");
                row.totalInvoiced = rs.getBigDecimal("total_invoiced");
                row.totalPaid = rs.getBigDecimal("total_paid");
                row.balance = row.totalInvoiced.subtract(row.totalPaid);
                rows.add(row);
            }
        }
        return rows;
    }
}
