package javaapplication1.erp.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Styled navigation button used in the sidebar.
 */
public class SidebarButton extends JButton {
    private boolean selected = false;
    private final String fullText;
    private static final int MAX_TEXT_WIDTH = Theme.SIDEBAR_WIDTH - Theme.SPACING_24 - 40; // Reduced for icon and padding

    public SidebarButton(String text, String iconName) {
        super(wrapTextInHtml(text, MAX_TEXT_WIDTH));
        this.fullText = text;
        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.TOP);
        Icon ic = UIUtils.icon(iconName, Theme.NAV_ICON_CIRCLE_SIZE);
        if (ic != null) setIcon(ic);
        setFocusable(false);
        setBackground(Theme.BACKGROUND);
        setForeground(Theme.TEXT_PRIMARY);
        setFont(Theme.FONT_BODY);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH - Theme.SPACING_16, -1)); // Let height auto-adjust
        setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH - Theme.SPACING_16, Integer.MAX_VALUE));
        setOpaque(true);
        setToolTipText(text);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selected) setBackground(Theme.HOVER_BACKGROUND);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!selected) setBackground(Theme.BACKGROUND);
            }
        });
    }

    public void setSelectedState(boolean sel) {
        this.selected = sel;
        setBackground(sel ? Theme.SELECTED_BACKGROUND : Theme.BACKGROUND);
        setForeground(sel ? Theme.PRIMARY_BLUE : Theme.TEXT_PRIMARY);
        repaint();
    }

    public void setCompact(boolean compact) {
        if (compact) {
            setText("");
            setHorizontalAlignment(SwingConstants.CENTER);
            setMaximumSize(new Dimension(92, Integer.MAX_VALUE));
        } else {
            setText(wrapTextInHtml(fullText, MAX_TEXT_WIDTH));
            setHorizontalAlignment(SwingConstants.LEFT);
            setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH - Theme.SPACING_16, Integer.MAX_VALUE));
        }
    }

    /**
     * Wrap text in HTML to enable word wrapping within button constraints.
     */
    private static String wrapTextInHtml(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // Use HTML to enable word wrapping. Width is set to approximate character count.
        return "<html><body style='width: " + maxWidth + "px; white-space: normal;'>" + text + "</body></html>";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!selected) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.PRIMARY_BLUE);
        g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
        g2.dispose();
    }
}
