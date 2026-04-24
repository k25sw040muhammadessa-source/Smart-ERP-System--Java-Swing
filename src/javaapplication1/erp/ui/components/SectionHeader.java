package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable section title with optional trailing action button.
 */
public class SectionHeader extends JPanel {

    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JButton actionButton;

    public SectionHeader(String title, String subtitle, JButton actionButton) {
        this.actionButton = actionButton;
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        titleLabel.setText(title);
        titleLabel.setFont(Theme.FONT_SUBHEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        left.add(titleLabel);

        subtitleLabel.setText(subtitle == null ? "" : subtitle);
        subtitleLabel.setFont(Theme.FONT_SMALL);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        left.add(subtitleLabel);

        add(left, BorderLayout.WEST);

        if (actionButton != null) {
            add(actionButton, BorderLayout.EAST);
        }
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle == null ? "" : subtitle);
    }

    public JButton getActionButton() {
        return actionButton;
    }
}
