package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.UserDAO;
import javaapplication1.erp.model.User;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javaapplication1.erp.util.DataAccessException;

public class UserDAOImpl implements UserDAO {
    private static final String SQL_FIND_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM users ORDER BY id DESC";
    private static final String SQL_SEARCH = "SELECT * FROM users WHERE username LIKE ? OR email LIKE ? ORDER BY id DESC";
    private static final String SQL_INSERT = "INSERT INTO users (username,password_hash,email,role_id,active) VALUES (?,?,?,?,?)";
    private static final String SQL_UPDATE = "UPDATE users SET username=?, email=?, role_id=?, active=? WHERE id=?";
    private static final String SQL_DELETE = "DELETE FROM users WHERE id=?";
    private static final String SQL_RESET_PASSWORD = "UPDATE users SET password_hash=? WHERE id=?";
    private static final String SQL_SET_ACTIVE = "UPDATE users SET active=? WHERE id=?";

    @Override
    public Optional<User> findByUsername(String username) throws Exception {
        try {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_USERNAME)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setPasswordHash(rs.getString("password_hash"));
                        u.setEmail(rs.getString("email"));
                        u.setRoleId(rs.getInt("role_id"));
                        u.setActive(rs.getBoolean("active"));
                        return Optional.of(u);
                    } else return Optional.empty();
                }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find user by username", ex);
        }
    }

    @Override
    public List<User> findAll() throws Exception {
        List<User> users = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to load users", ex);
        }
    }

    @Override
    public List<User> search(String term) throws Exception {
        List<User> users = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SEARCH)) {
            String q = "%" + term + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
            return users;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search users", ex);
        }
    }

    @Override
    public User create(User user, String plainPassword) throws Exception {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hashPassword(plainPassword));
            ps.setString(3, user.getEmail());
            ps.setInt(4, user.getRoleId());
            ps.setBoolean(5, user.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return user;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create user", ex);
        }
    }

    @Override
    public boolean update(User user) throws Exception {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getRoleId());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to update user", ex);
        }
    }

    @Override
    public boolean delete(int userId) throws Exception {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to delete user", ex);
        }
    }

    @Override
    public boolean resetPassword(int userId, String plainPassword) throws Exception {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_RESET_PASSWORD)) {
            ps.setString(1, PasswordUtil.hashPassword(plainPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to reset password", ex);
        }
    }

    @Override
    public boolean setActive(int userId, boolean active) throws Exception {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SET_ACTIVE)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to update user status", ex);
        }
    }

    private User mapRow(ResultSet rs) throws java.sql.SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEmail(rs.getString("email"));
        u.setRoleId(rs.getInt("role_id"));
        u.setActive(rs.getBoolean("active"));
        return u;
    }
}
