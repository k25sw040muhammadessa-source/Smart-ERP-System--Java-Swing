package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Navigation item component for sidebar.
 * Circular icon background with label, hover and selected states.
 */
public class NavItem extends Canvas implements MouseListener {

    private String label;
    private Image icon;
    private boolean hovered = false;
    private boolean selected = false;
    private Runnable onClick;
    private Image buffer;
    private float highlight = 0.0f;
    private float targetHighlight = 0.0f;
    private final Timer animator;

    public NavItem(String label, Image icon, Runnable onClick) {
        this.label = label;
        this.icon = icon;
        this.onClick = onClick;

        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH - 18, 56));
        addMouseListener(this);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        animator = new Timer(16, e -> animateHighlightStep());
        animator.setRepeats(true);
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public String getLabel() {
        return label;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        targetHighlight = selected ? 1.0f : (hovered ? 0.6f : 0.0f);
        startAnimation();
    }

    @Override
    public void paint(Graphics g) {
        paintToBuffer(g);
    }

    @Override
    public void update(Graphics g) {
        paintToBuffer(g);
    }

    private void paintToBuffer(Graphics g) {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        if (buffer == null || buffer.getWidth(this) != width || buffer.getHeight(this) != height) {
            buffer = createImage(width, height);
        }

        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setColor(Theme.SURFACE);
        g2d.fillRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        if (highlight > 0.02f) {
            int alpha = Math.min(255, (int) (120 * highlight));
            g2d.setColor(new Color(24, 119, 242, alpha));
            g2d.fillRoundRect(6, 6, width - 12, height - 12, 10, 10);
        }

        // Icon circle
        int iconX = Theme.SPACING_16;
        int iconY = (height - Theme.NAV_ICON_CIRCLE_SIZE) / 2;
        g2d.setColor(selected ? Theme.PRIMARY_BLUE : new Color(0xE8EEF7));
        g2d.fillOval(iconX, iconY, Theme.NAV_ICON_CIRCLE_SIZE, Theme.NAV_ICON_CIRCLE_SIZE);

        // Icon image
        if (icon != null) {
            int imgX = iconX + (Theme.NAV_ICON_CIRCLE_SIZE - Theme.NAV_ICON_SIZE) / 2;
            int imgY = iconY + (Theme.NAV_ICON_CIRCLE_SIZE - Theme.NAV_ICON_SIZE) / 2;
            Color iconTint = selected ? Color.WHITE : Theme.PRIMARY_BLUE;
            g2d.drawImage(tintIcon(icon, Theme.NAV_ICON_SIZE, iconTint), imgX, imgY, this);
        }

        // Label
        g2d.setColor(selected ? Theme.PRIMARY_BLUE.darker() : Theme.TEXT_PRIMARY);
        g2d.setFont(selected ? Theme.FONT_BODY_MEDIUM : Theme.FONT_BODY);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = iconX + Theme.NAV_ICON_CIRCLE_SIZE + Theme.SPACING_12;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(label, textX, textY);

        g2d.setColor(new Color(0xF1F2F4));
        g2d.drawLine(12, height - 1, width - 12, height - 1);

        g.drawImage(buffer, 0, 0, this);
        g2d.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        hovered = true;
        targetHighlight = selected ? 1.0f : 0.6f;
        startAnimation();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hovered = false;
        targetHighlight = selected ? 1.0f : 0.0f;
        startAnimation();
    }

    private void startAnimation() {
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    private void animateHighlightStep() {
        float diff = targetHighlight - highlight;
        if (Math.abs(diff) <= 0.01f) {
            highlight = targetHighlight;
            animator.stop();
            repaint();
            return;
        }
        highlight += diff * 0.22f;
        repaint();
    }

    private Image tintIcon(Image source, int size, Color tint) {
        BufferedImage tinted = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tinted.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(source, 0, 0, size, size, this);
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.setColor(tint);
        g2.fillRect(0, 0, size, size);
        g2.dispose();
        return tinted;
    }
}