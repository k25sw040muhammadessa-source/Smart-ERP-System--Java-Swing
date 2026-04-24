package javaapplication1.erp.ui.dashboard;

import javaapplication1.erp.ui.components.KpiCard;
import javaapplication1.erp.ui.components.SectionHeader;
import javaapplication1.erp.ui.components.StatusBadge;
import javaapplication1.erp.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main dashboard content panel with KPI cards, charts, tables, and activity feed.
 */
public class DashboardPanel extends JPanel {

    public interface DashboardActionListener {
        void openModule(String moduleKey);
    }

    private final DashboardDataProvider dataProvider;
    private final DashboardActionListener actionListener;

    private final JPanel kpiGrid = new JPanel(new GridLayout(1, 6, 16, 16));
    private final LineChartPanel salesTrendChart = new LineChartPanel("Sales Trend");
    private final BarChartPanel topProductChart = new BarChartPanel("Top Products");

    private final HoverTable recentSalesTable = new HoverTable();
    private final HoverTable lowStockTable = new HoverTable();

    private final DefaultTableModel recentSalesModel = new DefaultTableModel(
            new Object[]{"Invoice", "Customer", "Amount", "Status", "Date", "Action"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel lowStockModel = new DefaultTableModel(
            new Object[]{"Product", "Stock", "Reorder Level", "Supplier", "Status", "Action"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultListModel<String> activityModel = new DefaultListModel<>();
    private final JLabel loadingLabel = new JLabel(" ");

    private Consumer<Integer> notificationListener;

    public DashboardPanel(DashboardDataProvider dataProvider, DashboardActionListener actionListener) {
        this.dataProvider = dataProvider;
        this.actionListener = actionListener;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        kpiGrid.setOpaque(false);
        root.add(kpiGrid);
        root.add(Box.createVerticalStrut(16));

        JPanel chartRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartRow.setOpaque(false);
        chartRow.add(wrapInCard("Sales Trend (7 days)", "Daily revenue", salesTrendChart));
        chartRow.add(wrapInCard("Top Selling Products", "Units sold", topProductChart));
        root.add(chartRow);
        root.add(Box.createVerticalStrut(16));

        JPanel tableRow = new JPanel(new GridLayout(1, 2, 16, 0));
        tableRow.setOpaque(false);
        tableRow.add(createRecentSalesCard());
        tableRow.add(createLowStockCard());
        root.add(tableRow);
        root.add(Box.createVerticalStrut(16));

        root.add(createActivityCard());

        loadingLabel.setFont(Theme.FONT_SMALL);
        loadingLabel.setForeground(Theme.TEXT_MUTED);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 0));
        root.add(loadingLabel);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Theme.BACKGROUND);

        add(scroll, BorderLayout.CENTER);

        recentSalesTable.setModel(recentSalesModel);
        lowStockTable.setModel(lowStockModel);
        configureTables();
    }

    public void setNotificationListener(Consumer<Integer> notificationListener) {
        this.notificationListener = notificationListener;
    }

    public void refreshAsync(String filterType, String query, Runnable onDone) {
        setLoading(true, "Refreshing dashboard...");

        SwingWorker<DashboardModels.DashboardSnapshot, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardModels.DashboardSnapshot doInBackground() throws Exception {
                return dataProvider.loadSnapshot(filterType, query);
            }

            @Override
            protected void done() {
                try {
                    DashboardModels.DashboardSnapshot snapshot = get();
                    bindSnapshot(snapshot);
                    setLoading(false, "Last updated just now");
                } catch (Exception ex) {
                    setLoading(false, "Refresh failed. Showing previous data.");
                }

                if (onDone != null) {
                    onDone.run();
                }
            }
        };
        worker.execute();
    }

    private void setLoading(boolean loading, String text) {
        loadingLabel.setText(text);
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void bindSnapshot(DashboardModels.DashboardSnapshot snapshot) {
        bindKpis(snapshot.kpis());
        salesTrendChart.setData(snapshot.salesTrend());
        topProductChart.setData(snapshot.topProducts());
        bindRecentSales(snapshot.recentSales());
        bindLowStock(snapshot.lowStock());
        bindActivities(snapshot.activities());

        if (notificationListener != null) {
            notificationListener.accept(snapshot.pendingNotifications());
        }
    }

    private void bindKpis(List<DashboardModels.KpiMetric> metrics) {
        kpiGrid.removeAll();

        for (DashboardModels.KpiMetric metric : metrics) {
            KpiCard card = new KpiCard();
            card.bind(
                    metric.iconText(),
                    metric.label(),
                    metric.value(),
                    metric.trendText(),
                    toneToColor(metric.tone()),
                    () -> handleKpiClick(metric.id())
            );
            kpiGrid.add(card);
        }

        kpiGrid.revalidate();
        kpiGrid.repaint();
    }

    private void handleKpiClick(String id) {
        if (actionListener == null) {
            return;
        }
        if ("low_stock".equals(id)) {
            actionListener.openModule("lowStock");
        } else if ("pending_payments".equals(id)) {
            actionListener.openModule("payments");
        } else if ("products".equals(id)) {
            actionListener.openModule("products");
        } else if ("today_sales".equals(id)) {
            actionListener.openModule("sales");
        }
    }

    private JPanel createRecentSalesCard() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        recentSalesTable.setFillsViewportHeight(true);
        recentSalesTable.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(recentSalesTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        body.add(tableScroll, BorderLayout.CENTER);
        return wrapInCard("Recent Sales", "Last 10 invoices", body);
    }

    private JPanel createLowStockCard() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        lowStockTable.setFillsViewportHeight(true);
        lowStockTable.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(lowStockTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        body.add(tableScroll, BorderLayout.CENTER);
        return wrapInCard("Low Stock Items", "Needs reorder attention", body);
    }

    private JPanel createActivityCard() {
        JList<String> activityList = new JList<>(activityModel);
        activityList.setBackground(Theme.CARD_BACKGROUND);
        activityList.setFont(Theme.FONT_BODY);
        activityList.setForeground(Theme.TEXT_PRIMARY);
        activityList.setSelectionBackground(new Color(0xEFF6FF));

        JScrollPane scrollPane = new JScrollPane(activityList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        return wrapInCard("Recent Activity", "Latest system events", scrollPane);
    }

    private JPanel wrapInCard(String title, String subtitle, Component content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        SectionHeader header = new SectionHeader(title, subtitle, null);
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        body.add(content, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private void configureTables() {
        recentSalesTable.setRowHeight(34);
        lowStockTable.setRowHeight(34);

        recentSalesTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeCellRenderer());
        lowStockTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeCellRenderer());

        recentSalesTable.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        lowStockTable.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
    }

    private void bindRecentSales(List<DashboardModels.RecentSale> sales) {
        recentSalesModel.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (DashboardModels.RecentSale sale : sales) {
            recentSalesModel.addRow(new Object[]{
                    sale.invoiceNo(),
                    sale.customer(),
                    "$" + df.format(sale.amount()),
                    sale.status(),
                    dtf.format(sale.date()),
                    "View | Edit"
            });
        }
    }

    private void bindLowStock(List<DashboardModels.LowStockItem> items) {
        lowStockModel.setRowCount(0);

        for (DashboardModels.LowStockItem item : items) {
            lowStockModel.addRow(new Object[]{
                    item.product(),
                    item.stock(),
                    item.reorderLevel(),
                    item.supplier(),
                    item.status(),
                    "Reorder | View"
            });
        }
    }

    private void bindActivities(List<DashboardModels.ActivityItem> activities) {
        activityModel.clear();
        for (DashboardModels.ActivityItem activity : activities) {
            activityModel.addElement(activity.timestamp() + "  -  " + activity.message());
        }
    }

    private Color toneToColor(String tone) {
        if (tone == null) {
            return Theme.PRIMARY_BLUE;
        }
        switch (tone.toLowerCase()) {
            case "success":
                return Theme.SUCCESS_GREEN;
            case "warning":
                return Theme.WARNING;
            case "danger":
                return Theme.ERROR_RED;
            case "info":
                return Theme.INFO;
            default:
                return Theme.PRIMARY_BLUE;
        }
    }

    private static class HoverTable extends JTable {

        private int hoverRow = -1;

        HoverTable() {
            setShowGrid(false);
            setIntercellSpacing(new Dimension(0, 0));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setSelectionBackground(new Color(0xDBEAFE));
            setSelectionForeground(Theme.TEXT_PRIMARY);

            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    hoverRow = rowAtPoint(e.getPoint());
                    repaint();
                }
            });

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hoverRow = -1;
                    repaint();
                }
            });
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                if (row == hoverRow) {
                    c.setBackground(new Color(0xF8FAFC));
                } else {
                    c.setBackground(Color.WHITE);
                }
                c.setForeground(Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private static class StatusBadgeCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = String.valueOf(value);
            StatusBadge.Tone tone = StatusBadge.Tone.NEUTRAL;
            if ("paid".equalsIgnoreCase(text)) {
                tone = StatusBadge.Tone.SUCCESS;
            } else if ("pending".equalsIgnoreCase(text)) {
                tone = StatusBadge.Tone.WARNING;
            } else if ("unpaid".equalsIgnoreCase(text) || "critical".equalsIgnoreCase(text) || "low".equalsIgnoreCase(text)) {
                tone = StatusBadge.Tone.DANGER;
            }

            StatusBadge badge = new StatusBadge(text, tone);
            badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            p.setOpaque(true);
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            p.add(badge);
            return p;
        }
    }

    private static class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel(String.valueOf(value));
            lbl.setFont(Theme.FONT_SMALL);
            lbl.setForeground(Theme.PRIMARY_BLUE);

            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            p.setOpaque(true);
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            p.add(lbl);
            return p;
        }
    }

    private static class LineChartPanel extends JPanel {

        private final String title;
        private List<DashboardModels.ChartPoint> data = new ArrayList<>();

        LineChartPanel(String title) {
            this.title = title;
            setPreferredSize(new Dimension(400, 240));
            setOpaque(false);
        }

        void setData(List<DashboardModels.ChartPoint> data) {
            this.data = data == null ? List.of() : data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 36;
            int right = 16;
            int top = 18;
            int bottom = 26;

            g2.setColor(Theme.BORDER);
            g2.drawLine(left, h - bottom, w - right, h - bottom);
            g2.drawLine(left, top, left, h - bottom);

            double min = data.stream().mapToDouble(DashboardModels.ChartPoint::value).min().orElse(0);
            double max = data.stream().mapToDouble(DashboardModels.ChartPoint::value).max().orElse(1);
            if (max == min) {
                max = min + 1;
            }

            int n = data.size();
            int prevX = 0;
            int prevY = 0;
            g2.setColor(Theme.PRIMARY_BLUE);
            g2.setStroke(new BasicStroke(2f));

            for (int i = 0; i < n; i++) {
                DashboardModels.ChartPoint p = data.get(i);
                int x = left + i * (w - left - right) / Math.max(1, n - 1);
                double norm = (p.value() - min) / (max - min);
                int y = (int) (h - bottom - norm * (h - top - bottom));

                if (i > 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }

                g2.fillOval(x - 3, y - 3, 6, 6);
                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.TEXT_MUTED);
                g2.drawString(p.label(), x - 10, h - 8);
                g2.setColor(Theme.PRIMARY_BLUE);

                prevX = x;
                prevY = y;
            }

            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_SMALL);
            g2.drawString(title, left, 12);
            g2.dispose();
        }
    }

    private static class BarChartPanel extends JPanel {

        private final String title;
        private List<DashboardModels.ChartPoint> data = new ArrayList<>();

        BarChartPanel(String title) {
            this.title = title;
            setPreferredSize(new Dimension(400, 240));
            setOpaque(false);
        }

        void setData(List<DashboardModels.ChartPoint> data) {
            this.data = data == null ? List.of() : data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 24;
            int right = 12;
            int top = 20;
            int bottom = 30;

            double max = data.stream().mapToDouble(DashboardModels.ChartPoint::value).max().orElse(1);
            int count = data.size();
            int totalWidth = w - left - right;
            int barWidth = Math.max(14, totalWidth / (count * 2));
            int gap = barWidth;

            for (int i = 0; i < count; i++) {
                DashboardModels.ChartPoint p = data.get(i);
                int x = left + i * (barWidth + gap);
                int barHeight = (int) ((p.value() / max) * (h - top - bottom));
                int y = h - bottom - barHeight;

                g2.setColor(new Color(0x93C5FD));
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_SMALL);
                g2.drawString(p.label(), x - 2, h - 10);
            }

            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_SMALL);
            g2.drawString(title, left, 12);
            g2.dispose();
        }
    }
}
