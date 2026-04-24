package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.ProductDAO;
import javaapplication1.erp.model.Product;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public class ProductDAOImpl implements ProductDAO {
    private static final String INSERT_SQL = "INSERT INTO products (sku,name,description,price,cost,qty_in_stock,reorder_level,active) VALUES (?,?,?,?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM products WHERE id = ?";
    private static final String SELECT_BY_SKU = "SELECT * FROM products WHERE sku = ?";
    private static final String SEARCH_SQL = "SELECT * FROM products WHERE name LIKE ? OR sku LIKE ? LIMIT ? OFFSET ?";
    private static final String UPDATE_SQL = "UPDATE products SET sku=?,name=?,description=?,price=?,cost=?,qty_in_stock=?,reorder_level=?,active=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM products WHERE id = ?";

    @Override
    public Product create(Product p) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getSku());
                ps.setString(2, p.getName());
                ps.setString(3, p.getDescription());
                ps.setBigDecimal(4, p.getPrice() == null ? BigDecimal.ZERO : p.getPrice());
                ps.setBigDecimal(5, p.getCost() == null ? BigDecimal.ZERO : p.getCost());
                ps.setInt(6, p.getQtyInStock());
                ps.setInt(7, p.getReorderLevel());
                ps.setBoolean(8, p.isActive());
                int affected = ps.executeUpdate();
                if (affected == 0) throw new java.sql.SQLException("Creating product failed, no rows affected.");
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }
                return p;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create product", ex);
        }
    }

    @Override
    public Optional<Product> findById(int id) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find product by id", ex);
        }
    }

    @Override
    public Optional<Product> findBySKU(String sku) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(SELECT_BY_SKU)) {
                ps.setString(1, sku);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find product by SKU", ex);
        }
    }

    @Override
    public List<Product> search(String term, int limit, int offset) throws Exception {
        List<Product> list = new ArrayList<>();
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(SEARCH_SQL)) {
                String q = "%" + term + "%";
                ps.setString(1, q);
                ps.setString(2, q);
                ps.setInt(3, limit);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search products", ex);
        }
        return list;
    }

    @Override
    public List<Product> findAll() throws Exception {
        return search("", 1000, 0);
    }

    @Override
    public boolean update(Product p) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, p.getSku());
                ps.setString(2, p.getName());
                ps.setString(3, p.getDescription());
                ps.setBigDecimal(4, p.getPrice());
                ps.setBigDecimal(5, p.getCost());
                ps.setInt(6, p.getQtyInStock());
                ps.setInt(7, p.getReorderLevel());
                ps.setBoolean(8, p.isActive());
                ps.setInt(9, p.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to update product", ex);
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to delete product", ex);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setCost(rs.getBigDecimal("cost"));
        p.setQtyInStock(rs.getInt("qty_in_stock"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setActive(rs.getBoolean("active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
