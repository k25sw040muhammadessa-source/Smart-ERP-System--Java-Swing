package javaapplication1.erp.service;

import javaapplication1.erp.dao.UserDAO;
import javaapplication1.erp.model.User;
import javaapplication1.erp.util.PasswordUtil;

import java.util.Optional;

/**
 * AuthService handles login logic.
 */
public class AuthService {
    private final UserDAO userDao;

    public AuthService(UserDAO userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) throws Exception {
        Optional<User> ou = userDao.findByUsername(username);
        if (!ou.isPresent()) throw new Exception("Invalid credentials");
        User u = ou.get();
        if (!u.isActive()) throw new Exception("User is disabled");
        if (!PasswordUtil.verifyPassword(password, u.getPasswordHash())) throw new Exception("Invalid credentials");
        return u;
    }

    public Optional<User> authenticate(String username, String password) {
        try {
            return Optional.of(login(username, password));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
