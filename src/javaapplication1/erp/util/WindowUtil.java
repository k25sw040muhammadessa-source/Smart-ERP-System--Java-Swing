package javaapplication1.erp.util;

import java.awt.*;

/**
 * Utility methods for window management and scaling.
 */
public class WindowUtil {

    /**
     * Centers a window on the screen.
     */
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        // Prevent the window from being positioned off-screen (negative coordinates)
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        window.setLocation(x, y);
    }

    /**
     * Gets the DPI scaling factor.
     */
    public static double getDPIScale() {
        return Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
    }

    /**
     * Scales a dimension by DPI.
     */
    public static Dimension scaleDimension(Dimension dim) {
        double scale = getDPIScale();
        return new Dimension((int)(dim.width * scale), (int)(dim.height * scale));
    }

    /**
     * Scales a size by DPI.
     */
    public static int scaleSize(int size) {
        return (int)(size * getDPIScale());
    }
}