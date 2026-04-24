package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.CustomerDAO;
import javaapplication1.erp.model.Customer;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {
    private static final String INSERT_SQL = "INSERT INTO customers (name,contact_name,phone,email,address) VALUES (?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM customers WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM customers WHERE name LIKE ? OR contact_name LIKE ? LIMIT ? OFFSET ?";
    private static final String UPDATE_SQL = "UPDATE customers SET name=?,contact_name=?,phone=?,email=?,address=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM customers WHERE id = ?";

    @Override
    public Customer create(Customer c) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getContactName());
                ps.setString(3, c.getPhone());
                ps.setString(4, c.getEmail());
                ps.setString(5, c.getAddress());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) c.setId(rs.getInt(1)); }
                return c;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create customer", ex);
        }
    }

    @Override
    public Optional<Customer> findById(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find customer by id", ex);
        }
    }

    @Override
    public List<Customer> search(String term, int limit, int offset) throws Exception {
        List<Customer> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to search customers", ex);
        }
        return list;
    }

    @Override
    public boolean update(Customer c) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getContactName());
                ps.setString(3, c.getPhone());
                ps.setString(4, c.getEmail());
                ps.setString(5, c.getAddress());
                ps.setInt(6, c.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to update customer", ex);
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
            throw new DataAccessException("Failed to delete customer", ex);
        }
    }

    private Customer mapRow(ResultSet rs) throws java.sql.SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setContactName(rs.getString("contact_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        return c;
    }
}
