package javaapplication1.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil: hashing and verification using BCrypt.
 */
public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plain, String hash) {
        if (hash == null || hash.isEmpty()) return false;
        return BCrypt.checkpw(plain, hash);
    }
}
