package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * Custom rounded button component.
 * Supports hover, pressed states, icons, and keyboard focus.
 */
public class RoundedButton extends JComponent implements MouseListener, MouseMotionListener, KeyListener, FocusListener {

    private String text;
    private Image icon;
    private Runnable onClick;
    private boolean hovered = false;
    private boolean pressed = false;
    private boolean focused = false;
    private Color normalColor = Theme.PRIMARY_BLUE;
    private Color hoverColor = Theme.DARK_BLUE_ACCENT;
    private Color pressedColor = Theme.DARK_BLUE_ACCENT;
    private Color textColor = Color.WHITE;
    private Image buffer;

    public RoundedButton(String text) {
        this(text, null, null);
    }

    public RoundedButton(String text, Runnable onClick) {
        this(text, null, onClick);
    }

    public RoundedButton(String text, Image icon, Runnable onClick) {
        this.text = text;
        this.icon = icon;
        this.onClick = onClick;

        setPreferredSize(new Dimension(120, Theme.BUTTON_HEIGHT));
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addFocusListener(this);
        setFocusable(true);
        setBackground(new Color(0, 0, 0, 0));
    }

    public void setColors(Color normal, Color hover, Color pressed) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = pressed;
        repaint();
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintToBuffer(g);
    }

    private void paintToBuffer(Graphics g) {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        if (buffer == null || buffer.getWidth(this) != width || buffer.getHeight(this) != height) {
            buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Background
        boolean enabled = isEnabled();
        Color bgColor;
        if (enabled && pressed) {
            bgColor = pressedColor;
        } else if (enabled && hovered) {
            bgColor = hoverColor;
        } else {
            bgColor = normalColor;
        }

        // If disabled, use muted background and text
        Color drawTextColor = textColor;
        if (!enabled) {
            bgColor = new Color(0xF1F5F9);
            drawTextColor = Theme.TEXT_MUTED;
        }

        g2d.setColor(Theme.SHADOW);
        g2d.fillRoundRect(0, 2, width, height - 2, Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, width, height - 2, Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);

        // Border for focus (only when enabled)
        if (focused && enabled) {
            g2d.setColor(Theme.PRIMARY_BLUE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(1, 1, width - 3, height - 4, Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
        }

        // Text and icon
        g2d.setColor(drawTextColor);
        g2d.setFont(Theme.FONT_BODY_MEDIUM);

        FontMetrics fm = g2d.getFontMetrics();
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;

        int iconWidth = 0;
        if (icon != null) {
            iconWidth = Theme.NAV_ICON_SIZE;
            int iconX = Theme.COMPONENT_PADDING;
            int iconY = (height - Theme.NAV_ICON_SIZE) / 2;
            g2d.drawImage(icon, iconX, iconY, Theme.NAV_ICON_SIZE, Theme.NAV_ICON_SIZE, this);
        }

        int textX = icon != null ? Theme.COMPONENT_PADDING + iconWidth + Theme.SPACING_8 : (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, textX, textY);

        g.drawImage(buffer, 0, 0, this);
        g2d.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (contains(e.getPoint()) && onClick != null) {
            onClick.run();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;
        requestFocus();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressed = false;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hovered = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hovered = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (onClick != null) {
                onClick.run();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void focusGained(FocusEvent e) {
        focused = true;
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        focused = false;
        repaint();
    }
}