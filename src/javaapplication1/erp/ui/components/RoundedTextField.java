package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Custom rounded text field component using AWT Canvas.
 * Supports placeholder text, focus border, password masking, and show/hide toggle.
 */
public class RoundedTextField extends Canvas implements KeyListener, MouseListener, FocusListener {

    private String text = "";
    private String placeholder = "";
    private boolean focused = false;
    private boolean isPassword = false;
    private boolean showPassword = false;
    private boolean hasError = false;
    private Runnable onEnterPressed;
    private Image buffer;

    public RoundedTextField(String placeholder) {
        this(placeholder, false);
    }

    public RoundedTextField(String placeholder, boolean isPassword) {
        this.placeholder = placeholder;
        this.isPassword = isPassword;

        setPreferredSize(new Dimension(200, Theme.INPUT_HEIGHT));
        addKeyListener(this);
        addMouseListener(this);
        addFocusListener(this);
        setFocusable(true);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public void setError(boolean error) {
        this.hasError = error;
        repaint();
    }

    public void setOnEnterPressed(Runnable callback) {
        this.onEnterPressed = callback;
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
            buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, width - 1, height - 1, Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);

        // Border
        Color borderColor = hasError ? Theme.ERROR_RED : (focused ? Theme.PRIMARY_BLUE : Theme.BORDER);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(focused ? 2 : 1));
        g2d.drawRoundRect(focused ? 1 : 0, focused ? 1 : 0, width - (focused ? 3 : 1), height - (focused ? 3 : 1), Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);

        // Text
        g2d.setFont(Theme.FONT_BODY);
        FontMetrics fm = g2d.getFontMetrics();

        String displayText = text;
        if (isPassword && !showPassword) {
            displayText = "*".repeat(text.length());
        }

        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
        int textX = Theme.COMPONENT_PADDING;

        if (displayText.isEmpty() && !focused) {
            g2d.setColor(Theme.TEXT_MUTED);
            g2d.drawString(placeholder, textX, textY);
        } else {
            g2d.setColor(Theme.TEXT_PRIMARY);
            g2d.drawString(displayText, textX, textY);
        }

        // Password toggle
        if (isPassword) {
            int toggleSize = 16;
            int toggleX = width - Theme.COMPONENT_PADDING - toggleSize;
            int toggleY = (height - toggleSize) / 2;

            g2d.setColor(Theme.TEXT_MUTED);
            drawEyeIcon(g2d, toggleX, toggleY, toggleSize, showPassword);
        }

        g.drawImage(buffer, 0, 0, this);
        g2d.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (onEnterPressed != null) {
                onEnterPressed.run();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
            return;
        } else {
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126) {
            text += c;
            }
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocus();
        // Check if password toggle clicked
        if (isPassword) {
            int toggleSize = 16;
            int toggleX = getWidth() - Theme.COMPONENT_PADDING - toggleSize;
            int toggleY = (getHeight() - toggleSize) / 2;
            if (e.getX() >= toggleX && e.getX() <= toggleX + toggleSize && e.getY() >= toggleY && e.getY() <= toggleY + toggleSize) {
                showPassword = !showPassword;
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

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

    private void drawEyeIcon(Graphics2D g2d, int x, int y, int size, boolean open) {
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y + 4, size, size - 8);
        if (open) {
            g2d.fillOval(x + size / 2 - 2, y + size / 2 - 2, 4, 4);
        } else {
            g2d.drawLine(x + 2, y + size - 3, x + size - 2, y + 3);
        }
    }
}