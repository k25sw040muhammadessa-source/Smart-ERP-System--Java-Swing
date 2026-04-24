package javaapplication1.erp.model;

/**
 * User model
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private int roleId;
    private boolean active;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
