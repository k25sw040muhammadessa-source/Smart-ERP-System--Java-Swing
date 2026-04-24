package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.SupplierDAO;
import javaapplication1.erp.model.Supplier;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDAOImpl implements SupplierDAO {
    private static final String INSERT_SQL = "INSERT INTO suppliers (name,contact_name,phone,email,address) VALUES (?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM suppliers WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM suppliers WHERE name LIKE ? OR contact_name LIKE ? LIMIT ? OFFSET ?";
    private static final String UPDATE_SQL = "UPDATE suppliers SET name=?,contact_name=?,phone=?,email=?,address=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM suppliers WHERE id = ?";

    @Override
    public Supplier create(Supplier s) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, s.getName());
                ps.setString(2, s.getContactName());
                ps.setString(3, s.getPhone());
                ps.setString(4, s.getEmail());
                ps.setString(5, s.getAddress());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) s.setId(rs.getInt(1)); }
                return s;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create supplier", ex);
        }
    }

    @Override
    public Optional<Supplier> findById(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find supplier by id", ex);
        }
    }

    @Override
    public List<Supplier> search(String term, int limit, int offset) throws Exception {
        List<Supplier> list = new ArrayList<>();
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
                String q = "%" + term + "%";
                ps.setString(1, q);
                ps.setString(2, q);
                ps.setInt(3, limit);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search suppliers", ex);
        }
        return list;
    }

    @Override
    public boolean update(Supplier s) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, s.getName());
                ps.setString(2, s.getContactName());
                ps.setString(3, s.getPhone());
                ps.setString(4, s.getEmail());
                ps.setString(5, s.getAddress());
                ps.setInt(6, s.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to update supplier", ex);
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to delete supplier", ex);
        }
    }

    private Supplier mapRow(ResultSet rs) throws java.sql.SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setContactName(rs.getString("contact_name"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        return s;
    }
}
