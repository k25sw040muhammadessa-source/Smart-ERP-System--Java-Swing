package javaapplication1.erp.service;

import javaapplication1.erp.dao.UserDAO;
import javaapplication1.erp.model.User;
import javaapplication1.erp.util.ValidationUtil;

import java.util.List;

/**
 * Admin operations for users.
 */
public class UserService {
    private final UserDAO dao;

    public UserService(UserDAO dao) {
        this.dao = dao;
    }

    public List<User> search(String term) throws Exception {
        if (term == null || term.trim().isEmpty()) {
            return dao.findAll();
        }
        return dao.search(term.trim());
    }

    public User create(User user, String plainPassword) throws Exception {
        ValidationUtil.requireNonEmpty(user.getUsername(), "Username");
        ValidationUtil.requireEmail(user.getEmail(), "Email");
        ValidationUtil.requireNonEmpty(plainPassword, "Password");
        if (plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        return dao.create(user, plainPassword);
    }

    public boolean update(User user) throws Exception {
        ValidationUtil.requireNonEmpty(user.getUsername(), "Username");
        ValidationUtil.requireEmail(user.getEmail(), "Email");
        return dao.update(user);
    }

    public boolean delete(int userId) throws Exception {
        return dao.delete(userId);
    }

    public boolean resetPassword(int userId, String plainPassword) throws Exception {
        ValidationUtil.requireNonEmpty(plainPassword, "Password");
        if (plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        return dao.resetPassword(userId, plainPassword);
    }

    public boolean setActive(int userId, boolean active) throws Exception {
        return dao.setActive(userId, active);
    }
}
