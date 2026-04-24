package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Styled button component with primary and secondary variants.
 */
public class ModernButton extends JButton {

    public enum Variant { PRIMARY, SECONDARY }

    private final Variant variant;

    public ModernButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        initStyle();
    }

    private void initStyle() {
        setFocusPainted(false);
        setFont(Theme.FONT_BODY_MEDIUM);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 14, 8, 14));
        setPreferredSize(new Dimension(130, 38));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        if (variant == Variant.PRIMARY) {
            setBackground(Theme.PRIMARY_BLUE);
            setForeground(Color.WHITE);
            setOpaque(true);
        } else {
            setBackground(Theme.CARD_BACKGROUND);
            setForeground(Theme.TEXT_PRIMARY);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }
                if (variant == Variant.PRIMARY) {
                    setBackground(Theme.PRIMARY_HOVER);
                } else {
                    setBackground(Theme.SURFACE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (variant == Variant.PRIMARY) {
                    setBackground(Theme.PRIMARY_BLUE);
                } else {
                    setBackground(Theme.CARD_BACKGROUND);
                }
            }
        });
    }
}
