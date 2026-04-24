package javaapplication1.erp.util;

/**
 * Wrapper for JDBC-related exceptions to decouple DAOs from SQL specifics.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
