package javaapplication1.erp;

import javaapplication1.erp.app.Main;
import javaapplication1.erp.ui.theme.ThemeSetup;
import javaapplication1.erp.util.DatabaseBootstrap;

import javax.swing.*;

/**
 * Application entry point - updated to use AWT UI
 */
public class AppMain {
    public static void main(String[] args) {
        ThemeSetup.initLookAndFeel();

        try {
            DatabaseBootstrap.ensureSchemaAndSeed();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Database initialization failed: " + e.getMessage() + "\nCheck src/javaapplication1/resources/db.properties",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Start the new AWT-based UI
        Main.main(args);
    }
}
