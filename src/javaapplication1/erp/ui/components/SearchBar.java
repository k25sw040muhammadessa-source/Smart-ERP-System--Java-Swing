package javaapplication1.erp.ui.components;

import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Search input with optional type filter selector.
 */
public class SearchBar extends JPanel {

    private final JComboBox<String> filterCombo;
    private final JTextField searchField;

    public SearchBar() {
        setOpaque(false);
        setLayout(new BorderLayout(8, 0));

        filterCombo = new JComboBox<>(new String[]{"All", "Products", "Customers", "Invoices"});
        filterCombo.setFont(Theme.FONT_BODY);
        filterCombo.setPreferredSize(new Dimension(110, 36));

        searchField = new JTextField();
        searchField.setFont(Theme.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Search product / invoice / customer...");
        searchField.setPreferredSize(new Dimension(360, 36));

        add(filterCombo, BorderLayout.WEST);
        add(searchField, BorderLayout.CENTER);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        setBackground(Theme.CARD_BACKGROUND);
        setOpaque(true);
    }

    public JComboBox<String> getFilterCombo() {
        return filterCombo;
    }

    public JTextField getSearchField() {
        return searchField;
    }
}
