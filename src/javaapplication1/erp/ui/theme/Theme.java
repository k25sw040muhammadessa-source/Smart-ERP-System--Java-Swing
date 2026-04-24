package javaapplication1.erp.ui.theme;

import java.awt.*;

/**
 * Theme constants for the Smart ERP application.
 */
public class Theme {

    // Core palette
    public static final Color PRIMARY_BLUE = new Color(0x2563EB);
    public static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
    public static final Color SUCCESS_GREEN = new Color(0x10B981);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color ERROR_RED = new Color(0xEF4444);
    public static final Color INFO = new Color(0x06B6D4);
    public static final Color BACKGROUND = new Color(0xF8FAFC);
    public static final Color CARD_BACKGROUND = new Color(0xFFFFFF);
    public static final Color SURFACE = CARD_BACKGROUND;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color BORDERS = BORDER;
    public static final Color TEXT_MUTED = new Color(0x64748B);
    public static final Color TEXT_PRIMARY = new Color(0x0F172A);
    public static final Color HOVER_BACKGROUND = new Color(0xEFF6FF);
    public static final Color SELECTED_BACKGROUND = new Color(0xDBEAFE);
    public static final Color TOPBAR_BACKGROUND = new Color(0xFFFFFF);
    public static final Color SHADOW = new Color(15, 23, 42, 20);
    public static final Color DARK_BLUE_ACCENT = PRIMARY_HOVER;

    // Typography
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY_MEDIUM = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_LINK = new Font("SansSerif", Font.PLAIN, 11);

    // 8px spacing system
    public static final int SPACING_8 = 6;
    public static final int SPACING_12 = 9;
    public static final int SPACING_16 = 12;
    public static final int SPACING_24 = 16;
    public static final int SPACING_32 = 20;

    // Backward compatibility aliases
    public static final int SPACING_XS = SPACING_8;
    public static final int SPACING_SM = 9;
    public static final int SPACING_MD = SPACING_16;
    public static final int SPACING_LG = SPACING_24;
    public static final int SPACING_XL = SPACING_32;

    // Component dimensions
    public static final int CORNER_RADIUS = 10;
    public static final int INPUT_HEIGHT = 32;
    public static final int BUTTON_HEIGHT = 32;
    public static final int SIDEBAR_WIDTH = 240;
    public static final int NAV_ICON_SIZE = 18;
    public static final int NAV_ICON_CIRCLE_SIZE = 30;

    // Panel padding
    public static final int PANEL_PADDING = 10;
    public static final int COMPONENT_PADDING = 8;

    // Table
    public static final int TABLE_ROW_HEIGHT = 24;
    public static final int TABLE_HEADER_HEIGHT = 28;
}