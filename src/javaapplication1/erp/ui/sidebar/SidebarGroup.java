package javaapplication1.erp.ui.sidebar;

import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Collapsible sidebar menu group with a section header and child buttons.
 */
public class SidebarGroup extends JPanel {

    private final JToggleButton headerButton = new JToggleButton();
    private final JPanel itemsPanel = new JPanel();
    private final List<JComponent> items = new ArrayList<>();

    public SidebarGroup(String title) {
        setOpaque(false);
        setLayout(new BorderLayout());

        headerButton.setText(title);
        headerButton.setSelected(true);
        headerButton.setHorizontalAlignment(SwingConstants.LEFT);
        headerButton.setFocusPainted(false);
        headerButton.setFont(Theme.FONT_BODY_MEDIUM);
        headerButton.setForeground(Theme.TEXT_MUTED);
        headerButton.setBackground(Theme.BACKGROUND);
        headerButton.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8)); // Reduced padding
        headerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        headerButton.addActionListener(this::toggleGroup);

        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));

        add(headerButton, BorderLayout.NORTH);
        add(itemsPanel, BorderLayout.CENTER);
        updateHeaderText();
    }

    public void addItem(JComponent item) {
        items.add(item);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, item.getPreferredSize().height));
        itemsPanel.add(item);
        itemsPanel.add(Box.createVerticalStrut(2)); // Reduced spacing to fit more items
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void setCollapsed(boolean collapsed) {
        headerButton.setSelected(!collapsed);
        itemsPanel.setVisible(!collapsed);
        updateHeaderText();
    }

    private void toggleGroup(ActionEvent e) {
        itemsPanel.setVisible(headerButton.isSelected());
        updateHeaderText();
        revalidate();
        repaint();
    }

    private void updateHeaderText() {
        String text = headerButton.getText();
        if (text.startsWith("[")) {
            text = text.substring(text.indexOf(']') + 1).trim();
        }
        headerButton.setText((headerButton.isSelected() ? "[-] " : "[+] ") + text);
    }
}
