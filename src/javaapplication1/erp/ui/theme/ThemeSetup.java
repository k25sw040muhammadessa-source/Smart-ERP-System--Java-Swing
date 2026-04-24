package javaapplication1.erp.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.BorderFactory;

/**
 * Centralized Look and Feel setup for Swing screens.
 */
public final class ThemeSetup {

    private static volatile boolean initialized;

    private ThemeSetup() {
    }

    public static void initLookAndFeel() {
        if (initialized) {
            return;
        }

        synchronized (ThemeSetup.class) {
            if (initialized) {
                return;
            }
            try {
                FlatLightLaf.setup();
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("TextComponent.arc", 10);
                UIManager.put("ScrollBar.thumbArc", 999);
                UIManager.put("ScrollBar.showButtons", false);
                UIManager.put("Table.showHorizontalLines", false);
                UIManager.put("Table.showVerticalLines", false);
                UIManager.put("Table.background", Theme.CARD_BACKGROUND);
                UIManager.put("Table.foreground", Theme.TEXT_PRIMARY);
                UIManager.put("Table.selectionBackground", Theme.SELECTED_BACKGROUND);
                UIManager.put("Table.selectionForeground", Theme.TEXT_PRIMARY);
                UIManager.put("Table.gridColor", Theme.BORDER);
                UIManager.put("Table.focusCellBackground", Theme.SELECTED_BACKGROUND);
                UIManager.put("Table.focusCellForeground", Theme.TEXT_PRIMARY);
                UIManager.put("Table.rowHeight", Theme.TABLE_ROW_HEIGHT);
                UIManager.put("TableHeader.background", Theme.BACKGROUND);
                UIManager.put("TableHeader.foreground", Theme.TEXT_PRIMARY);
                UIManager.put("TableHeader.separatorColor", Theme.BORDER);
                UIManager.put("TableHeader.bottomSeparatorColor", Theme.BORDER);
                UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(Theme.BORDER));
            } catch (Exception ignored) {
                // Falls back to platform LAF if FlatLaf setup fails.
            }
            initialized = true;
        }
    }
}
