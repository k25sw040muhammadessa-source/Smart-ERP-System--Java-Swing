package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.InventoryMovementDAO;
import javaapplication1.erp.model.InventoryMovement;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryMovementDAOImpl implements InventoryMovementDAO {
    private static final String INSERT_SQL = "INSERT INTO inventory_movements (product_id, qty, type, ref_type, ref_id, note) VALUES (?,?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM inventory_movements WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM inventory_movements WHERE product_id = ? ORDER BY movement_date DESC LIMIT ? OFFSET ?";
    private static final String DELETE_SQL = "DELETE FROM inventory_movements WHERE id = ?";

    @Override
    public InventoryMovement create(InventoryMovement im) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, im.getProductId());
                ps.setInt(2, im.getQty());
                ps.setString(3, im.getType());
                ps.setString(4, im.getRefType());
                if (im.getRefId() == null) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, im.getRefId());
                ps.setString(6, im.getNote());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) im.setId(rs.getInt(1)); }
                return im;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create inventory movement", ex);
        }
    }

    @Override
    public Optional<InventoryMovement> findById(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find inventory movement by id", ex);
        }
    }

    @Override
    public List<InventoryMovement> search(String term, int limit, int offset) throws Exception {
        // 'term' here is expected to be productId as string
        int pid = 0;
        try { pid = Integer.parseInt(term); } catch (Exception e) { return new ArrayList<>(); }
        List<InventoryMovement> list = new ArrayList<>();
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
                ps.setInt(1, pid);
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search inventory movements", ex);
        }
        return list;
    }

    @Override
    public boolean update(InventoryMovement im) throws Exception { throw new UnsupportedOperationException("Update not supported for inventory movements"); }

    @Override
    public boolean delete(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to delete inventory movement", ex);
        }
    }

    private InventoryMovement mapRow(ResultSet rs) throws java.sql.SQLException {
        InventoryMovement im = new InventoryMovement();
        im.setId(rs.getInt("id"));
        im.setProductId(rs.getInt("product_id"));
        im.setQty(rs.getInt("qty"));
        im.setType(rs.getString("type"));
        im.setRefType(rs.getString("ref_type"));
        int r = rs.getInt("ref_id"); im.setRefId(rs.wasNull() ? null : r);
        im.setMovementDate(rs.getTimestamp("movement_date"));
        im.setNote(rs.getString("note"));
        return im;
    }
}
