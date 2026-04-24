package javaapplication1.erp.ui.theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Small helper utilities to apply Theme styling to common Swing components.
 */
public class UIUtils {

    public static Icon icon(String name, int size) {
        java.awt.Image img = Assets.getIconScaled(name, size);
        if (img == null) return null;
        return new ImageIcon(img);
    }

    public static void styleTable(JTable table) {
        if (table == null) return;
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.SELECTED_BACKGROUND);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(Theme.TABLE_ROW_HEIGHT);
        table.setFont(Theme.FONT_BODY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setBackground(Theme.CARD_BACKGROUND);
        table.setOpaque(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setBackground(isSelected ? Theme.SELECTED_BACKGROUND : (row % 2 == 0 ? Theme.CARD_BACKGROUND : new Color(0xF8FBFF)));
                component.setForeground(Theme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return component;
            }
        });
        table.setRowHeight(Theme.TABLE_ROW_HEIGHT);
        JTableHeader th = table.getTableHeader();
        if (th != null) {
            th.setPreferredSize(new Dimension(th.getPreferredSize().width, Theme.TABLE_HEADER_HEIGHT));
            th.setFont(Theme.FONT_BODY_MEDIUM);
            th.setBackground(Theme.PRIMARY_BLUE);
            th.setForeground(Color.WHITE);
        }
    }

    public static void styleButton(JButton btn) {
        if (btn == null) return;
        btn.setBackground(Theme.PRIMARY_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(Theme.FONT_BODY_MEDIUM);
        FontMetrics metrics = btn.getFontMetrics(Theme.FONT_BODY_MEDIUM);
        int textWidth = metrics.stringWidth(btn.getText() == null ? "" : btn.getText());
        int targetWidth = Math.max(120, textWidth + 52);
        Dimension size = new Dimension(targetWidth, Theme.BUTTON_HEIGHT);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setFocusPainted(false);
    }

    public static void styleTextField(JTextField tf) {
        if (tf == null) return;
        tf.setFont(Theme.FONT_BODY);
        tf.setPreferredSize(new Dimension(120, Theme.INPUT_HEIGHT));
        tf.setBackground(Theme.CARD_BACKGROUND);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    public static void styleInput(JTextComponent input) {
        if (input == null) return;
        input.setFont(Theme.FONT_BODY);
        input.setBackground(Theme.CARD_BACKGROUND);
        input.setForeground(Theme.TEXT_PRIMARY);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        if (comboBox == null) return;
        comboBox.setFont(Theme.FONT_BODY);
        comboBox.setBackground(Theme.CARD_BACKGROUND);
        comboBox.setForeground(Theme.TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        comboBox.setPreferredSize(new Dimension(120, Theme.INPUT_HEIGHT));
    }

    public static void styleLabel(JLabel label) {
        if (label == null) return;
        label.setFont(Theme.FONT_BODY_MEDIUM);
        label.setForeground(Theme.TEXT_PRIMARY);
    }

    public static void styleFormControls(Container root) {
        if (root == null) return;
        for (Component component : root.getComponents()) {
            if (component instanceof JTable || component instanceof JTableHeader) {
                continue;
            }
            if (component instanceof JButton button) {
                styleButton(button);
            } else if (component instanceof JTextField textField) {
                styleTextField(textField);
            } else if (component instanceof JPasswordField passwordField) {
                styleInput(passwordField);
            } else if (component instanceof JComboBox<?> comboBox) {
                styleComboBox(comboBox);
            } else if (component instanceof JCheckBox checkBox) {
                checkBox.setFont(Theme.FONT_BODY);
                checkBox.setForeground(Theme.TEXT_PRIMARY);
                checkBox.setOpaque(false);
            } else if (component instanceof JLabel label) {
                styleLabel(label);
            }
            if (component instanceof Container child) {
                styleFormControls(child);
            }
        }
    }
}
