package com.redcode.mcms.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Passwords are never stored in plain text. This utility uses bcrypt hashing,
 * which automatically includes a per-user random salt.
 */
public final class PasswordHasher {

    private PasswordHasher() { }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
