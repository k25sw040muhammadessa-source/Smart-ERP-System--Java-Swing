package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.EmployeeDAO;
import javaapplication1.erp.model.Employee;
import javaapplication1.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javaapplication1.erp.util.DataAccessException;

public class EmployeeDAOImpl implements EmployeeDAO {
    // Basic SQL placeholders - expand as needed
    private static final String INSERT_SQL = "INSERT INTO employees (first_name,last_name,email,phone,hire_date,department) VALUES (?,?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM employees WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM employees WHERE first_name LIKE ? OR last_name LIKE ? LIMIT ? OFFSET ?";
    private static final String UPDATE_SQL = "UPDATE employees SET first_name=?,last_name=?,email=?,phone=?,hire_date=?,department=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM employees WHERE id = ?";

    @Override
    public Employee create(Employee e) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getFirstName());
                ps.setString(2, e.getLastName());
                ps.setString(3, e.getEmail());
                ps.setString(4, e.getPhone());
                ps.setDate(5, e.getHireDate() == null ? null : new java.sql.Date(e.getHireDate().getTime()));
                ps.setString(6, e.getDepartment());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) e.setId(rs.getInt(1)); }
                return e;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create employee", ex);
        }
    }

    @Override
    public Optional<Employee> findById(int id) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Employee e = mapRow(rs);
                        return Optional.of(e);
                    }
                }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find employee by id", ex);
        }
    }

    @Override
    public List<Employee> search(String term, int limit, int offset) throws Exception {
        List<Employee> list = new ArrayList<>();
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(SEARCH_SQL)) {
                String q = "%" + term + "%";
                ps.setString(1, q);
                ps.setString(2, q);
                ps.setInt(3, limit);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search employees", ex);
        }
        return list;
    }

    @Override
    public boolean update(Employee e) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, e.getFirstName());
                ps.setString(2, e.getLastName());
                ps.setString(3, e.getEmail());
                ps.setString(4, e.getPhone());
                ps.setDate(5, e.getHireDate() == null ? null : new java.sql.Date(e.getHireDate().getTime()));
                ps.setString(6, e.getDepartment());
                ps.setInt(7, e.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to update employee", ex);
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
            throw new DataAccessException("Failed to delete employee", ex);
        }
    }

    private Employee mapRow(ResultSet rs) throws java.sql.SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setHireDate(rs.getDate("hire_date"));
        e.setDepartment(rs.getString("department"));
        return e;
    }
}
