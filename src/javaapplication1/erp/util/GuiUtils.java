package javaapplication1.erp.util;

import javax.swing.*;

/**
 * Small GUI helpers for consistent error/info dialogs.
 */
public final class GuiUtils {
    private GuiUtils() {}

    public static void showError(Throwable t) {
        String msg = t == null ? "Unknown error" : t.getMessage();
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
