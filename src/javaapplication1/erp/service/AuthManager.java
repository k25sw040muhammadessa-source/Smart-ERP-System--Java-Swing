package javaapplication1.erp.service;

import javaapplication1.erp.dao.UserDAO;
import javaapplication1.erp.dao.impl.UserDAOImpl;
import javaapplication1.erp.model.User;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.PasswordUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Lightweight in-memory auth manager for desktop demo usage.
 */
public final class AuthManager {

    private static final UserDAO USER_DAO = new UserDAOImpl();
    private static final List<ResetRequest> RESET_REQUESTS = new ArrayList<>();

    /** Fetch role_id by name from DB — fallback to 2 if DB unavailable */
    private static int getRoleIdByName(String roleName) {
        String sql = "SELECT id FROM roles WHERE name = ? LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return "Admin".equalsIgnoreCase(roleName) ? 1 : 2;
    }
    private static final List<AuditEntry> AUDIT_TRAIL = new ArrayList<>();
    private static int requestSeq = 1001;

    private AuthManager() {
    }

    public static synchronized LoginResult authenticate(String username, String password) {
        if (username == null || password == null) {
            return new LoginResult(false, false, "Invalid credentials.");
        }

        String user = username.trim().toLowerCase();
        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(user);
            if (maybeUser.isEmpty()) {
                return new LoginResult(false, false, "Invalid username or password.");
            }

            User dbUser = maybeUser.get();
            if (!PasswordUtil.verifyPassword(password, dbUser.getPasswordHash())) {
                return new LoginResult(false, false, "Invalid username or password.");
            }

            if (!dbUser.isActive()) {
                return new LoginResult(false, false, "Account pending admin approval.");
            }
        } catch (Exception ex) {
            return new LoginResult(false, false, "Login failed. Please try again.");
        }

        boolean forceChange = false;
        String note = "";
        for (int i = RESET_REQUESTS.size() - 1; i >= 0; i--) {
            ResetRequest request = RESET_REQUESTS.get(i);
            if (request.account.equals(user) && "APPROVED".equals(request.status) && password.equals(request.tempPassword)) {
                forceChange = true;
                note = "Temporary password active. Please set a new password.";
                break;
            }
        }

        audit("LOGIN_SUCCESS", user, "User logged in");
        return new LoginResult(true, forceChange, note);
    }

    public static synchronized boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        try {
            return USER_DAO.findByUsername(username.trim().toLowerCase()).isPresent();
        } catch (Exception ex) {
            return false;
        }
    }

    public static synchronized RequestResult registerUser(String username, String email, String password) {
        if (username == null || username.trim().length() < 3) {
            return new RequestResult(false, "Username must be at least 3 characters.");
        }
        if (password == null || password.trim().length() < 4) {
            return new RequestResult(false, "Password must be at least 4 characters.");
        }
        if (email == null || !email.contains("@") || email.length() < 5) {
            return new RequestResult(false, "Enter a valid email.");
        }

        String user = username.trim().toLowerCase();
        String normalizedEmail = email.trim().toLowerCase();

        if (userExists(user)) {
            return new RequestResult(false, "Username already exists.");
        }

        if (emailExists(normalizedEmail)) {
            return new RequestResult(false, "Email is already registered.");
        }

        try {
            User newUser = new User();
            newUser.setUsername(user);
            newUser.setEmail(normalizedEmail);
            newUser.setRoleId(getRoleIdByName("User")); // dynamically fetched
            newUser.setActive(false);
            USER_DAO.create(newUser, password.trim());
        } catch (Exception ex) {
            return new RequestResult(false, "Unable to create account right now.");
        }

        audit("USER_REGISTERED", user, "User created account");
        return new RequestResult(true, "Account created. Wait for admin approval.");
    }

    public static synchronized RequestResult approveRegisteredUser(String username, String approver, String approverPassword) {
        String user = username == null ? "" : username.trim().toLowerCase();

        LoginResult login = authenticate(approver, approverPassword);
        if (!login.success) {
            return new RequestResult(false, "Admin approval failed.");
        }
        // Check that approver is actually an Admin role (not just username "admin")
        int approverRoleId = getUserRole(approver);
        int adminRoleId = getRoleIdByName("Admin");
        if (approverRoleId != adminRoleId) {
            return new RequestResult(false, "Only admins can approve users.");
        }

        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(user);
            if (maybeUser.isEmpty()) {
                return new RequestResult(false, "User not found.");
            }
            User target = maybeUser.get();
            USER_DAO.setActive(target.getId(), true);
        } catch (Exception ex) {
            return new RequestResult(false, "Failed to approve user.");
        }

        audit("USER_APPROVED", user, "Approved by admin");
        return new RequestResult(true, "User approved successfully.");
    }

    public static synchronized boolean isUserApproved(String username) {
        if (username == null) {
            return false;
        }
        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(username.trim().toLowerCase());
            return maybeUser.map(User::isActive).orElse(false);
        } catch (Exception ex) {
            return false;
        }
    }

    public static synchronized int getUserRole(String username) {
        if (username == null) {
            return 0;
        }
        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(username.trim().toLowerCase());
            return maybeUser.map(User::getRoleId).orElse(0);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static synchronized List<SignupRequest> getPendingSignupRequests() {
        List<SignupRequest> requests = new ArrayList<>();
        int userRoleId = getRoleIdByName("User");
        String sql = "SELECT username, email, role_id, active FROM users WHERE active = 0 AND role_id = ? AND username <> ? ORDER BY id DESC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userRoleId);
            ps.setString(2, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(new SignupRequest(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getInt("role_id"),
                            rs.getBoolean("active")
                    ));
                }
            }
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(requests);
    }

    public static synchronized RequestResult rejectRegisteredUser(String username, String approver, String approverPassword, String reason) {
        String user = username == null ? "" : username.trim().toLowerCase();

        LoginResult login = authenticate(approver, approverPassword);
        if (!login.success || getUserRole(approver) != getRoleIdByName("Admin")) {
            return new RequestResult(false, "Admin approval failed.");
        }

        if ("admin".equalsIgnoreCase(user)) {
            return new RequestResult(false, "Admin account cannot be rejected.");
        }

        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(user);
            if (maybeUser.isEmpty()) {
                return new RequestResult(false, "User not found.");
            }
            USER_DAO.delete(maybeUser.get().getId());
        } catch (Exception ex) {
            return new RequestResult(false, "Failed to reject signup request.");
        }

        audit("USER_REJECTED", user, reason == null || reason.isBlank() ? "Rejected by admin" : reason.trim());
        return new RequestResult(true, "Signup request rejected.");
    }

    public static synchronized RequestResult submitResetRequest(String account, String reason) {
        if (!userExists(account)) {
            return new RequestResult(false, "Account does not exist.");
        }

        ResetRequest request = new ResetRequest(
                "REQ-" + requestSeq++,
                account.trim().toLowerCase(),
                reason == null ? "" : reason.trim(),
                "PENDING",
                LocalDateTime.now(),
                null,
                "",
                "",
                ""
        );
        RESET_REQUESTS.add(request);
        audit("RESET_REQUEST_SUBMITTED", request.account, "Request " + request.id + " submitted");
        return new RequestResult(true, "Request submitted. Admin approval is required.");
    }

    public static synchronized RequestResult approveResetRequest(String requestId, String approver, String approverPassword) {
        ResetRequest request = findRequestById(requestId);
        if (request == null) {
            return new RequestResult(false, "Reset request not found.");
        }
        if (!"PENDING".equals(request.status)) {
            return new RequestResult(false, "Only pending requests can be approved.");
        }

        LoginResult login = authenticate(approver, approverPassword);
        if (!login.success || getUserRole(approver) != getRoleIdByName("Admin")) {
            return new RequestResult(false, "Admin approval failed.");
        }

        String tempPassword = generateTempPassword();
        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(request.account);
            if (maybeUser.isEmpty()) {
                return new RequestResult(false, "Account does not exist.");
            }
            USER_DAO.resetPassword(maybeUser.get().getId(), tempPassword);
        } catch (Exception ex) {
            return new RequestResult(false, "Unable to approve reset request.");
        }
        request.status = "APPROVED";
        request.approvedAt = LocalDateTime.now();
        request.approvedBy = "admin";
        request.tempPassword = tempPassword;
        request.note = "Approved with temporary password.";
        audit("RESET_REQUEST_APPROVED", request.account, request.id + " approved by admin");
        return new RequestResult(true, "Approved. Temporary password: " + tempPassword);
    }

    public static synchronized RequestResult rejectResetRequest(String requestId, String approver, String approverPassword, String rejectionReason) {
        ResetRequest request = findRequestById(requestId);
        if (request == null) {
            return new RequestResult(false, "Reset request not found.");
        }
        if (!"PENDING".equals(request.status)) {
            return new RequestResult(false, "Only pending requests can be rejected.");
        }

        LoginResult login = authenticate(approver, approverPassword);
        if (!login.success || getUserRole(approver) != getRoleIdByName("Admin")) {
            return new RequestResult(false, "Admin approval failed.");
        }

        request.status = "REJECTED";
        request.approvedAt = LocalDateTime.now();
        request.approvedBy = "admin";
        request.note = rejectionReason == null ? "Rejected" : rejectionReason.trim();
        audit("RESET_REQUEST_REJECTED", request.account, request.id + " rejected by admin");
        return new RequestResult(true, "Request rejected.");
    }

    public static synchronized RequestResult changePasswordAfterTempLogin(String username, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 4) {
            return new RequestResult(false, "New password must have at least 4 characters.");
        }

        LoginResult loginResult = authenticate(username, currentPassword);
        if (!loginResult.success) {
            return new RequestResult(false, "Current password is incorrect.");
        }

        String user = username == null ? "" : username.trim().toLowerCase();
        try {
            Optional<User> maybeUser = USER_DAO.findByUsername(user);
            if (maybeUser.isEmpty()) {
                return new RequestResult(false, "Account does not exist.");
            }
            USER_DAO.resetPassword(maybeUser.get().getId(), newPassword.trim());
        } catch (Exception ex) {
            return new RequestResult(false, "Unable to change password.");
        }

        for (ResetRequest request : RESET_REQUESTS) {
            if (request.account.equals(user) && "APPROVED".equals(request.status)) {
                request.status = "COMPLETED";
                request.note = "User changed password after temporary login.";
            }
        }
        audit("PASSWORD_CHANGED", user, "Password changed after OTP login");
        return new RequestResult(true, "Password changed successfully.");
    }

    public static synchronized List<ResetRequest> getResetRequests() {
        return Collections.unmodifiableList(new ArrayList<>(RESET_REQUESTS));
    }

    public static synchronized List<AuditEntry> getAuditTrail() {
        return Collections.unmodifiableList(new ArrayList<>(AUDIT_TRAIL));
    }

    private static void audit(String action, String actor, String details) {
        AUDIT_TRAIL.add(new AuditEntry(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                action,
                actor,
                details
        ));
        // Also persist to DB so audit logs survive restarts
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO audit_logs (user_id, action, entity_name, entity_id, details) " +
                 "SELECT id, ?, 'Auth', NULL, ? FROM users WHERE username = ? " +
                 "UNION ALL SELECT NULL, ?, 'Auth', NULL, ? WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = ?) LIMIT 1")) {
            ps.setString(1, action);
            ps.setString(2, details);
            ps.setString(3, actor);
            ps.setString(4, action);
            ps.setString(5, details);
            ps.setString(6, actor);
            ps.executeUpdate();
        } catch (Exception ignored) { /* DB audit failure must never break auth */ }
    }

    private static String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder("TMP-");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static ResetRequest findRequestById(String id) {
        for (ResetRequest request : RESET_REQUESTS) {
            if (request.id.equals(id)) {
                return request;
            }
        }
        return null;
    }

    private static boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ? LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public static final class LoginResult {
        public final boolean success;
        public final boolean forcePasswordChange;
        public final String message;

        public LoginResult(boolean success, boolean forcePasswordChange, String message) {
            this.success = success;
            this.forcePasswordChange = forcePasswordChange;
            this.message = message;
        }
    }

    public static final class RequestResult {
        public final boolean success;
        public final String message;

        public RequestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static final class SignupRequest {
        public final String username;
        public final String email;
        public final int roleId;
        public final boolean active;

        public SignupRequest(String username, String email, int roleId, boolean active) {
            this.username = username;
            this.email = email;
            this.roleId = roleId;
            this.active = active;
        }
    }

    public static final class ResetRequest {
        public final String id;
        public final String account;
        public final String reason;
        public String status;
        public final LocalDateTime submittedAt;
        public LocalDateTime approvedAt;
        public String approvedBy;
        public String tempPassword;
        public String note;

        public ResetRequest(String id, String account, String reason, String status, LocalDateTime submittedAt, LocalDateTime approvedAt, String approvedBy, String tempPassword, String note) {
            this.id = id;
            this.account = account;
            this.reason = reason;
            this.status = status;
            this.submittedAt = submittedAt;
            this.approvedAt = approvedAt;
            this.approvedBy = approvedBy;
            this.tempPassword = tempPassword;
            this.note = note;
        }
    }

    public static final class AuditEntry {
        public final String timestamp;
        public final String action;
        public final String actor;
        public final String details;

        public AuditEntry(String timestamp, String action, String actor, String details) {
            this.timestamp = timestamp;
            this.action = action;
            this.actor = actor;
            this.details = details;
        }
    }
}