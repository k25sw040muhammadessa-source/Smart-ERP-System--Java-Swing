package javaapplication1.erp.ui;

import javaapplication1.erp.dao.impl.EmployeeDAOImpl;
import javaapplication1.erp.model.Employee;
import javaapplication1.erp.service.EmployeeService;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.ui.theme.AnimatedPanel;
import javaapplication1.erp.ui.theme.PlaceholderTextField;
import javaapplication1.erp.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EmployeeListPanel extends JPanel {
    private final EmployeeService service = new EmployeeService(new EmployeeDAOImpl());
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "First Name", "Last Name", "Email", "Phone", "Department", "Hire Date"}, 0);
    private final JTable table = new JTable(tableModel);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final JTextField txtFirstName = new PlaceholderTextField("e.g. John", 18);
    private final JTextField txtLastName = new PlaceholderTextField("e.g. Smith", 18);
    private final JTextField txtEmail = new PlaceholderTextField("e.g. john.smith@example.com", 18);
    private final JTextField txtPhone = new PlaceholderTextField("john.smith@example.com", 14);
    private final JTextField txtDepartment = new PlaceholderTextField("e.g. Sales", 16);
    private final JTextField txtHireDate = new PlaceholderTextField("yyyy-MM-dd", 12);
    private final JTextField txtSearch = new JTextField(20);

    private final JButton btnSearch = new JButton("Search");
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");

    public EmployeeListPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.BACKGROUND);
        
        // Create animated form container
        JPanel formContent = buildForm();
        AnimatedPanel animatedForm = new AnimatedPanel(new BorderLayout());
        animatedForm.setPreferredSize(new Dimension(460, 560));
        animatedForm.setBorder(new EmptyBorder(8, 8, 8, 8));
        animatedForm.add(formContent, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animatedForm, buildTablePanel());
        split.setDividerLocation(460);
        add(split, BorderLayout.CENTER);
            // Apply styling
            UIUtils.styleTable(table);
            UIUtils.styleButton(btnAdd);
            UIUtils.styleButton(btnUpdate);
            UIUtils.styleButton(btnDelete);
            UIUtils.styleButton(btnSearch);
            UIUtils.styleTextField(txtFirstName);
            UIUtils.styleTextField(txtLastName);
            UIUtils.styleTextField(txtEmail);
            UIUtils.styleTextField(txtPhone);
            UIUtils.styleTextField(txtDepartment);
            UIUtils.styleTextField(txtHireDate);
            UIUtils.styleTextField(txtSearch);
            bindActions();
            loadEmployees("");
    }

    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 23, 42, 25));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 10, 18, 18);

                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 16, 16);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, 16, 16);

                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(24, 28, 24, 28));
        cardPanel.setPreferredSize(new Dimension(410, 530));

        JLabel titleLabel = new JLabel("Employee Details");
        titleLabel.setFont(Theme.FONT_SUBHEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        cardPanel.add(createFieldBlock("First Name", txtFirstName));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Last Name", txtLastName));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Email", txtEmail));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Phone", txtPhone));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Department", txtDepartment));
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(createFieldBlock("Hire Date", txtHireDate));
        cardPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnAdd.setFont(Theme.FONT_BODY_MEDIUM);
        btnAdd.setPreferredSize(new Dimension(90, 38));
        btnUpdate.setFont(Theme.FONT_BODY_MEDIUM);
        btnUpdate.setPreferredSize(new Dimension(90, 38));
        btnDelete.setFont(Theme.FONT_BODY_MEDIUM);
        btnDelete.setPreferredSize(new Dimension(90, 38));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        cardPanel.add(buttonPanel);

        wrapper.add(cardPanel, new GridBagConstraints());
        return wrapper;
    }

    private JPanel createFieldBlock(String labelText, JTextField field) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(Component.CENTER_ALIGNMENT);
        block.setMaximumSize(new Dimension(330, 62));

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY_MEDIUM);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(Theme.FONT_BODY);
        field.setPreferredSize(new Dimension(320, 36));
        field.setMaximumSize(new Dimension(320, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);

        return block;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        top.add(txtSearch);
        top.add(btnSearch);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void bindActions() {
        btnSearch.addActionListener(e -> loadEmployees(txtSearch.getText().trim()));
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtFirstName.setText(valueAt(row, 1));
                txtLastName.setText(valueAt(row, 2));
                txtEmail.setText(valueAt(row, 3));
                txtPhone.setText(valueAt(row, 4));
                txtDepartment.setText(valueAt(row, 5));
                txtHireDate.setText(valueAt(row, 6));
            }
        });
    }

    private String valueAt(int row, int col) {
        Object value = tableModel.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private Employee readForm() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName(txtFirstName.getText().trim());
        employee.setLastName(txtLastName.getText().trim());
        employee.setEmail(txtEmail.getText().trim());
        employee.setPhone(txtPhone.getText().trim());
        employee.setDepartment(txtDepartment.getText().trim());
        Date hireDate = dateFormat.parse(txtHireDate.getText().trim());
        employee.setHireDate(hireDate);
        return employee;
    }

    private void loadEmployees(String term) {
        SwingWorker<List<Employee>, Void> worker = new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() throws Exception {
                return service.search(term, 200, 0);
            }

            @Override
            protected void done() {
                try {
                    List<Employee> employees = get();
                    tableModel.setRowCount(0);
                    for (Employee employee : employees) {
                        tableModel.addRow(new Object[]{
                                employee.getId(),
                                employee.getFirstName(),
                                employee.getLastName(),
                                employee.getEmail(),
                                employee.getPhone(),
                                employee.getDepartment(),
                                employee.getHireDate() == null ? "" : dateFormat.format(employee.getHireDate())
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeListPanel.this, "Failed to load employees: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onAdd() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.createEmployee(readForm());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadEmployees("");
                    JOptionPane.showMessageDialog(EmployeeListPanel.this, "Employee added");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeListPanel.this, "Add failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first");
            return;
        }
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Employee employee = readForm();
                employee.setId((int) tableModel.getValueAt(row, 0));
                return service.updateEmployee(employee);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadEmployees("");
                        JOptionPane.showMessageDialog(EmployeeListPanel.this, "Employee updated");
                    } else {
                        JOptionPane.showMessageDialog(EmployeeListPanel.this, "No changes were saved");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeListPanel.this, "Update failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected employee?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        int employeeId = (int) tableModel.getValueAt(row, 0);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return new EmployeeDAOImpl().delete(employeeId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadEmployees("");
                        JOptionPane.showMessageDialog(EmployeeListPanel.this, "Employee deleted");
                    } else {
                        JOptionPane.showMessageDialog(EmployeeListPanel.this, "Delete failed");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeListPanel.this, "Delete failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void applySearch(String term) {
        txtSearch.setText(term == null ? "" : term);
        loadEmployees(txtSearch.getText().trim());
    }

    public void refreshData() {
        txtSearch.setText("");
        loadEmployees("");
    }
}
