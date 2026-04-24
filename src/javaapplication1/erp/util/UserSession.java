package javaapplication1.erp.util;

import javaapplication1.erp.model.User;

/**
 * Simple singleton session holder.
 */
public class UserSession {
    private static UserSession instance;
    private User user;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isLoggedIn() { return user != null; }
    public void logout() { user = null; }
    public boolean hasPermission(Permission permission) {
        return AccessControl.hasPermission(user, permission);
    }
}
