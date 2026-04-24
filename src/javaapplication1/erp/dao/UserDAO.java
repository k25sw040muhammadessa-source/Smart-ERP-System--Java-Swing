package javaapplication1.erp.dao;

import javaapplication1.erp.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username) throws Exception;
    List<User> findAll() throws Exception;
    List<User> search(String term) throws Exception;
    User create(User user, String plainPassword) throws Exception;
    boolean update(User user) throws Exception;
    boolean delete(int userId) throws Exception;
    boolean resetPassword(int userId, String plainPassword) throws Exception;
    boolean setActive(int userId, boolean active) throws Exception;
}
