package javaapplication1.erp.ui.screens;

import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.WindowUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "Help", true);
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 520);
        setResizable(false);
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        WindowUtil.centerWindow(this);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(20, 22, 20, 22));
        root.setBackground(Theme.BACKGROUND);

        JLabel title = new JLabel("How to Use Smart ERP", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.PRIMARY_BLUE);
        root.add(title, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(
            "Complete Help & User Guide for Smart ERP\n\n" +
            "Quick Start\n" +
            "1) Prepare database: ensure MySQL is running and update src/javaapplication1/resources/db.properties with your DB host, port, database name, user and password.\n" +
            "2) Build and run: open in NetBeans and Run, or use Ant / command-line as documented in About.\n\n" +
            "Login Screen\n" +
            "- Fields: 'Email / Username' and 'Password'. Enter credentials and press Enter or click LOGIN.\n" +
            "- 'Forgot Password?': sends a reset request; admin will approve or reject from System → Pending Requests.\n" +
            "- 'New here? Sign Up': creates a pending user account that requires admin approval.\n\n" +
            "Dashboard Overview\n" +
            "- The dashboard provides summary widgets for sales, revenue, stock levels and recent activity.\n" +
            "- Use the sidebar (left) to navigate modules — click a module to open its panel.\n\n" +
            "Navigation & Sidebar\n" +
            "- Sidebar groups: Master, Operations, Finance, Reports, System. Expand a group to see its items.\n" +
            "- Selecting a sidebar item loads an interactive table or form. The currently selected module name is shown in the top bar.\n\n" +
            "Top Bar Buttons (detailed)\n" +
            "- Back: returns to the previous content view (only enabled if there is history). Disabled when unavailable.\n" +
            "- About: opens project information. Use this to check version, dependencies and build instructions.\n" +
            "- Help: opens this guide (you are here). Contains step-by-step workflows and button behaviors.\n" +
            "- Logout: signs the current user out and returns to login. Unsaved changes may be lost, so save before logging out.\n" +
            "- Exit: closes the application process immediately.\n\n" +
            "Common Button Types & Behaviors\n" +
            "- Primary action buttons (filled blue): perform the main action for the screen (Save, Create, Generate Invoice, Approve). They show hover/pressed states and are disabled when action is not valid.\n" +
            "- Secondary actions (muted / outline / grey): auxiliary actions such as Cancel, Back or View Details.\n" +
            "- Dangerous actions (red): Delete, Reject — a confirmation dialog is usually shown before finalizing.\n" +
            "- Focus & Keyboard: most forms allow pressing Enter to submit. Buttons show a blue focus outline when focused; disabled buttons are grey/muted.\n\n" +
            "Module Workflows (step-by-step)\n" +
            "Products / Master Data\n" +
            "- Add Product: open Products → Click New / Add → fill fields (name, SKU, unit, category, purchase/sale price, tax) → Save.\n" +
            "- Edit Product: select a row, click Edit / Update → modify fields → Save.\n" +
            "- Delete Product: select a row → Delete → confirm. Deleted items may be soft-deleted depending on DB design.\n\n" +
            "Customers & Suppliers\n" +
            "- Create contact: open Customer or Supplier → New → enter contact details, payment terms, default currency → Save.\n" +
            "- Search: use search box or filters to locate existing contacts.\n\n" +
            "Inventory Movements\n" +
            "- Stock In / Out: open Inventory → Create Movement → choose product, quantity, movement type (in/out/adjustment), reference note → Save.\n" +
            "- Low Stock: use Low Stock panel to identify products below reorder level and create purchase requests.\n\n" +
            "Sales Workflow Example\n" +
            "1) Open Sales → New Sales Order.\n" +
            "2) Select or create a Customer.\n" +
            "3) Add products: search product, set quantity, unit price, tax and discount (if applicable).\n" +
            "4) Review totals and apply discounts or shipping charges.\n" +
            "5) Save Order → optionally Generate Invoice (PDF).\n" +
            "6) Record Payment: open Payments → New Payment → link to invoice and record payment method (cash/bank).\n\n" +
            "Purchases Workflow (mirror of Sales)\n" +
            "- Create Purchase Order → add supplier and products → receive stock via Inventory / Goods Receipt → record supplier invoice and payment.\n\n" +
            "Payments and Ledger\n" +
            "- Payments Module records receipts and payments with references to invoices or suppliers.\n" +
            "- Ledger provides a running view of account balances and transaction details. Use filters to narrow by date, account, customer or supplier.\n\n" +
            "Reports & Export\n" +
            "- Sales Report: generates period-based sales summaries.\n" +
            "- Inventory Report: stock levels, valuation and movement history.\n" +
            "- Export/Print: many screens allow exporting lists or printing details; Invoice PDF uses the PDF generator service.\n\n" +
            "User Management, Signup & Approvals\n" +
            "- New user signups create pending requests that admin must approve from System → Pending Requests.\n" +
            "- Admins assign roles and permissions in Permission Management. Use the grid to toggle allowed actions for each role.\n\n" +
            "Reset Password Flow\n" +
            "- Users request resets using Forgot Password. Requests appear to admins in pending list. Admins can approve or set temporary passwords.\n\n" +
            "Troubleshooting & Common Errors\n" +
            "- DB Connection failed: check src/javaapplication1/resources/db.properties and that MySQL is reachable at configured host and port.\n" +
            "- Blank screens or missing icons: ensure src/icons/logo.png exists and is readable; check console logs for image IO errors.\n" +
            "- Permission errors: verify user role has required permissions in System → Permission Management.\n\n" +
            "Tips & Best Practices\n" +
            "- Save before navigating away from forms.\n" +
            "- When updating or deleting records, always select the correct row first.\n" +
            "- Use the dashboard to monitor short-term issues like low stock or pending approvals.\n\n" +
            "Advanced Administration\n" +
            "- Backup DB: use mysqldump against the configured database.\n" +
            "- Restore: import SQL using mysql client.\n" +
            "- Configuration: advanced JDBC params can be set in db.params in db.properties.\n\n" +
            "If you want a printable or expanded PDF manual with screenshots, tell me which sections to include and I will generate a formatted document."
        );
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(Theme.FONT_BODY);
        textArea.setBackground(Theme.CARD_BACKGROUND);
        textArea.setForeground(Theme.TEXT_PRIMARY);
        textArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        root.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(closeButton);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        setLocationRelativeTo(getOwner());
    }
}