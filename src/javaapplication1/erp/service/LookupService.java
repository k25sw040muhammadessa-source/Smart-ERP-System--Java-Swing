package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared lightweight lookup queries for selector controls.
 */
public class LookupService {
    public static class LookupRow {
        public int id;
        public String label;

        @Override
        public String toString() {
            return id + " - " + label;
        }
    }

    public List<LookupRow> activeProducts() throws Exception {
        return query("SELECT id, CONCAT(sku, ' | ', name) label FROM products WHERE active = 1 ORDER BY name");
    }

    public List<LookupRow> customers() throws Exception {
        return query("SELECT id, name label FROM customers ORDER BY name");
    }

    public List<LookupRow> suppliers() throws Exception {
        return query("SELECT id, name label FROM suppliers ORDER BY name");
    }

    private List<LookupRow> query(String sql) throws Exception {
        List<LookupRow> rows = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LookupRow row = new LookupRow();
                row.id = rs.getInt("id");
                row.label = rs.getString("label");
                rows.add(row);
            }
        }
        return rows;
    }
}
