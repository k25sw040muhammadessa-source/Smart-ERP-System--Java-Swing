package javaapplication1.erp.ui;

import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;

/**
 * Reusable structural template for CRUD panels of remaining entities.
 */
public abstract class EntityCrudPanelTemplate extends JPanel {
    protected final JTextField txtSearch = new JTextField(22);
    protected final JButton btnSearch = new JButton("Search");
    protected final JButton btnAdd = new JButton("Add");
    protected final JButton btnUpdate = new JButton("Update");
    protected final JButton btnDelete = new JButton("Delete");
    protected final DefaultTableModel tableModel = new DefaultTableModel(getTableColumns(), 0);
    protected final JTable table = new JTable(tableModel);

    protected EntityCrudPanelTemplate() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.BACKGROUND);
        UIUtils.styleTable(table);
        
        // Set font and preferredSize for buttons to prevent text overflow
        btnSearch.setFont(Theme.FONT_BODY_MEDIUM);
        btnSearch.setPreferredSize(new Dimension(80, Theme.BUTTON_HEIGHT));
        btnAdd.setFont(Theme.FONT_BODY_MEDIUM);
        btnAdd.setPreferredSize(new Dimension(80, Theme.BUTTON_HEIGHT));
        btnUpdate.setFont(Theme.FONT_BODY_MEDIUM);
        btnUpdate.setPreferredSize(new Dimension(80, Theme.BUTTON_HEIGHT));
        btnDelete.setFont(Theme.FONT_BODY_MEDIUM);
        btnDelete.setPreferredSize(new Dimension(80, Theme.BUTTON_HEIGHT));
        
        txtSearch.setFont(Theme.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(200, Theme.BUTTON_HEIGHT));
        add(buildTopPanel(), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(sp, BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.WEST);
        bindActions();
        reloadTable("");
    }

    protected abstract Object[] getTableColumns();

    protected abstract JPanel buildTopPanel();

    protected abstract JPanel buildFormPanel();

    protected abstract void bindActions();

    protected abstract void reloadTable(String keyword);
}
