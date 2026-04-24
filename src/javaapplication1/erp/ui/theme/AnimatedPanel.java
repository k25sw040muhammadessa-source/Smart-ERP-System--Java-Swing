package javaapplication1.erp.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * AnimatedPanel: A custom JPanel with fade-in animation and light blue background.
 * Used to wrap form content with smooth appearance animation.
 */
public class AnimatedPanel extends JPanel {
    private float alpha = 0f;
    private final int animationDuration = 100; // milliseconds
    private final Timer animationTimer;

    public AnimatedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        
        // Create animation timer
        animationTimer = new Timer(16, this::animate); // ~60fps
        animationTimer.start();
    }

    private void animate(ActionEvent e) {
        if (alpha < 1f) {
            long elapsed = System.currentTimeMillis();
            alpha = Math.min(1f, alpha + (16f / animationDuration));
            repaint();
        } else {
            animationTimer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw light blue background with current alpha
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(new Color(219, 237, 254)); // Light blue: #DBEDFE
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        // Draw border
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(191, 219, 254, (int)(200 * alpha))); // Border blue with alpha
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        super.paintComponent(g);
    }
}
