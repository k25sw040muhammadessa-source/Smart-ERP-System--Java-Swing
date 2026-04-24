package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.PurchaseDAO;
import javaapplication1.erp.model.Purchase;
import javaapplication1.erp.model.PurchaseItem;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseDAOImpl implements PurchaseDAO {
    private static final String INSERT_SQL = "INSERT INTO purchases (purchase_no,supplier_id,purchase_date,total_amount,status) VALUES (?,?,?,?,?)";
    private static final String INSERT_ITEM_SQL = "INSERT INTO purchase_items (purchase_id,product_id,qty,unit_cost,line_total) VALUES (?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM purchases WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM purchases WHERE supplier_id = ? LIMIT ? OFFSET ?";
    private static final String DELETE_SQL = "DELETE FROM purchases WHERE id = ?";

    @Override
    public Purchase create(Purchase p) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    String purchaseNo = "PO-LEGACY-" + System.currentTimeMillis();
                    ps.setString(1, purchaseNo);
                    ps.setInt(2, p.getSupplierId());
                    ps.setDate(3, p.getPurchaseDate() == null ? null : new java.sql.Date(p.getPurchaseDate().getTime()));
                    ps.setDouble(4, p.getTotalAmount());
                    ps.setString(5, p.getStatus());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) p.setId(rs.getInt(1)); }
                }
                // insert items
                if (p.getItems() != null) {
                    try (PreparedStatement psi = conn.prepareStatement(INSERT_ITEM_SQL)) {
                        for (PurchaseItem it : p.getItems()) {
                            psi.setInt(1, p.getId());
                            psi.setInt(2, it.getProductId());
                            psi.setInt(3, it.getQty());
                            psi.setDouble(4, it.getPrice());
                            psi.setDouble(5, it.getQty() * it.getPrice());
                            psi.addBatch();
                        }
                        psi.executeBatch();
                    }
                }
                conn.commit();
                conn.setAutoCommit(true);
                return p;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create purchase", ex);
        }
    }

    @Override
    public Optional<Purchase> findById(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find purchase by id", ex);
        }
    }

    @Override
    public List<Purchase> search(String term, int limit, int offset) throws Exception {
        List<Purchase> list = new ArrayList<>();
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
                ps.setInt(1, Integer.parseInt(term));
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search purchases", ex);
        }
        return list;
    }

    @Override
    public boolean update(Purchase p) throws Exception { throw new UnsupportedOperationException("Update purchases via business flow"); }

    @Override
    public boolean delete(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to delete purchase", ex);
        }
    }

    private Purchase mapRow(ResultSet rs) throws java.sql.SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setPurchaseDate(rs.getDate("purchase_date"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setStatus(rs.getString("status"));
        // items can be fetched by separate query if needed
        return p;
    }
}
