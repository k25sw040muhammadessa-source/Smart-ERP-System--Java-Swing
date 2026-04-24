package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clickable KPI card for dashboard summary metrics.
 */
public class KpiCard extends JPanel {

    private final JLabel iconLabel = new JLabel();
    private final JLabel valueLabel = new JLabel("0");
    private final JLabel titleLabel = new JLabel("Metric");
    private final JLabel trendLabel = new JLabel("0% vs last period");
    private Color accentColor = Theme.PRIMARY_BLUE;
    private Runnable onClick;

    public KpiCard() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.CARD_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        iconLabel.setForeground(Theme.TEXT_MUTED);
        top.add(iconLabel, BorderLayout.WEST);

        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        top.add(titleLabel, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(Box.createVerticalStrut(8));
        center.add(valueLabel);

        trendLabel.setFont(Theme.FONT_SMALL);
        trendLabel.setForeground(Theme.TEXT_MUTED);
        trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(Box.createVerticalStrut(6));
        center.add(trendLabel);

        add(center, BorderLayout.CENTER);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accentColor, 1),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER, 1),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public void bind(String iconText, String title, String value, String trend, Color accentColor, Runnable onClick) {
        this.accentColor = accentColor;
        this.onClick = onClick;
        iconLabel.setText(iconText);
        iconLabel.setForeground(accentColor);
        titleLabel.setText(title);
        valueLabel.setText(value);
        trendLabel.setText(trend);
        trendLabel.setForeground(accentColor.equals(Theme.ERROR_RED) ? Theme.ERROR_RED : Theme.TEXT_MUTED);
    }
}
