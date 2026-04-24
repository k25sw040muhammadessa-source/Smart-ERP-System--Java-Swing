package javaapplication1.erp.app;

import javaapplication1.erp.ui.screens.DashboardFrame;
import javaapplication1.erp.ui.screens.LoginFrame;
import javaapplication1.erp.ui.screens.SplashScreenFrame;
import javaapplication1.erp.ui.theme.ThemeSetup;

/**
 * Main application entry point.
 * Starts with splash screen, then login, then dashboard.
 */
public class Main {

    public static void main(String[] args) {
        ThemeSetup.initLookAndFeel();

        java.awt.EventQueue.invokeLater(() -> {
            final LoginFrame[] loginHolder = new LoginFrame[1];
            final DashboardFrame[] dashboardHolder = new DashboardFrame[1];

            loginHolder[0] = new LoginFrame(() -> {
                dashboardHolder[0] = new DashboardFrame(() -> {
                    if (dashboardHolder[0] != null) {
                        dashboardHolder[0].dispose();
                        dashboardHolder[0] = null;
                    }
                    if (loginHolder[0] != null) {
                        loginHolder[0].setVisible(true);
                        loginHolder[0].toFront();
                    }
                });
                loginHolder[0].setVisible(false);
                dashboardHolder[0].setVisible(true);
            });

            new SplashScreenFrame(() -> loginHolder[0].setVisible(true));
        });
    }
}