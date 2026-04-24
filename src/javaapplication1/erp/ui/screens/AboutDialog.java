package javaapplication1.erp.ui.screens;

import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.WindowUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, "About Smart ERP", true);
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 360);
        setResizable(false);
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        WindowUtil.centerWindow(this);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(20, 22, 20, 22));
        root.setBackground(Theme.BACKGROUND);

        JLabel title = new JLabel("About This Project", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.PRIMARY_BLUE);
        root.add(title, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(
            "Smart ERP System — Desktop Edition\n\n" +
            "Version: v2.0 Premium Edition\n" +
            "Author: Muhammad Essa (Roll Number K25SW040)\n\n" +
            "Overview:\n" +
            "Smart ERP is a lightweight desktop application built to manage core business operations for small and medium enterprises. " +
            "It provides modules for products, inventory, sales, purchases, customers, suppliers, users and permissions, finance/ledger views, " +
            "and PDF invoice generation. The user interface uses Swing with custom components to provide a modern desktop experience.\n\n" +
            "Key Features:\n" +
            "- Product catalog: create, edit, search, categorize and track items.\n" +
            "- Inventory movements: record stock in/out, adjust quantities and get low-stock alerts.\n" +
            "- Sales: create sales orders, generate invoices, apply payments and export PDF invoices.\n" +
            "- Purchases: create purchase orders, manage supplier invoices and stock receipts.\n" +
            "- Customers & Suppliers: full CRUD with search and contact data.\n" +
            "- Users & Roles: signup workflow, admin approval, role-based permissions and password reset requests.\n" +
            "- Ledger & Payments: record transactions and view ledger balances.\n" +
            "- Reporting: generate simple sales, inventory and ledger reports from the dashboard.\n\n" +
            "Architecture & Technology Stack:\n" +
            "- Java (JDK 11+) with Swing/AWT for UI.\n" +
            "- Layered architecture: DAOs for persistence, Services for business logic, UI panels for presentation.\n" +
            "- MySQL via JDBC for data storage.\n" +
            "- Password hashing with jBCrypt.\n" +
            "- PDF generation using OpenPDF.\n" +
            "- FlatLaf for modern look-and-feel (bundled as a jar in lib/).\n\n" +
            "Important Files & Locations:\n" +
            "- Sources: src/ (Java source code by package).\n" +
            "- Icons: src/icons/ (application logos and icons).\n" +
            "- Database config: src/javaapplication1/resources/db.properties (edit db.host, db.port, db.name, db.user, db.password).\n" +
            "- Libraries: lib/ (third-party jars used by the app).\n\n" +
            "Build & Run Instructions:\n" +
            "- Preferred: Open the project in NetBeans, then Clean & Build and Run.\n" +
            "- Ant: ant -f build.xml clean; ant -f build.xml compile; ant -f build.xml run.\n" +
            "- Command-line (manual): generate build/sources_compiled.txt listing all source files, then: \n" +
            "  javac -d build/classes -cp \"lib/*\" @build/sources_compiled.txt\n" +
            "  java -cp \"build/classes;lib/*\" javaapplication1.erp.app.Main\n\n" +
            "Third-Party Libraries (bundled in lib/):\n" +
            "- flatlaf-3.x (UI look and feel)\n" +
            "- jbcrypt (password hashing)\n" +
            "- mysql-connector-j (JDBC driver)\n" +
            "- openpdf (PDF generation)\n\n" +
            "Support, Contribution & License:\n" +
            "- This is a student/educational project. Use and modify it for learning and internal demo purposes. For redistribution or commercial use, obtain permission from the author.\n" +
            "- To contribute: modify source under src/, follow existing package patterns (dao, service, ui, model), and test locally.\n\n" +
            "Acknowledgements:\n" +
            "- Project created as part of coursework and demonstrates basic ERP functionality and a layered Java application architecture."
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