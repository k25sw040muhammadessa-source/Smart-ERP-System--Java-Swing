package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Rounded status badge for table cells and notification indicators.
 */
public class StatusBadge extends JLabel {

    public enum Tone {
        SUCCESS(Theme.SUCCESS_GREEN, new Color(0xECFDF5)),
        WARNING(Theme.WARNING, new Color(0xFFFBEB)),
        DANGER(Theme.ERROR_RED, new Color(0xFEF2F2)),
        INFO(Theme.INFO, new Color(0xECFEFF)),
        NEUTRAL(Theme.TEXT_MUTED, new Color(0xF1F5F9));

        final Color foreground;
        final Color background;

        Tone(Color foreground, Color background) {
            this.foreground = foreground;
            this.background = background;
        }
    }

    private Tone tone;

    public StatusBadge(String text, Tone tone) {
        super(text);
        this.tone = tone;
        setFont(Theme.FONT_SMALL);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        setForeground(tone.foreground);
    }

    public void setTone(Tone tone) {
        this.tone = tone;
        setForeground(tone.foreground);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(tone.background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}
