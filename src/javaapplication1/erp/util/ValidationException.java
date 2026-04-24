package javaapplication1.erp.util;

/**
 * Thrown for validation errors in UI/service layer.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
