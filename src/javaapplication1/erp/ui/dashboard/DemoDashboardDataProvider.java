package javaapplication1.erp.ui.dashboard;

import javaapplication1.erp.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demo provider that mixes real lightweight KPIs and seeded dashboard data.
 */
public class DemoDashboardDataProvider implements DashboardDataProvider {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    public DashboardModels.DashboardSnapshot loadSnapshot(String filterType, String query) {
        Map<String, String> kpiMap = dashboardService.fetchKpis();

        List<DashboardModels.KpiMetric> kpis = new ArrayList<>();
        kpis.add(new DashboardModels.KpiMetric("today_sales", "Today Sales", "$" + kpiMap.get("todaySales"), "+8.2% vs yesterday", "$", "info"));
        kpis.add(new DashboardModels.KpiMetric("month_revenue", "This Month Revenue", "$128,450", "+12.7% vs last month", "R", "success"));
        kpis.add(new DashboardModels.KpiMetric("low_stock", "Low Stock Items", kpiMap.get("lowStockCount"), "Needs reorder", "!", "danger"));
        kpis.add(new DashboardModels.KpiMetric("pending_payments", "Pending Payments", "$18,900", "7 overdue invoices", "P", "warning"));
        kpis.add(new DashboardModels.KpiMetric("products", "Total Products", kpiMap.get("products"), "+6 new this week", "B", "info"));
        kpis.add(new DashboardModels.KpiMetric("customers", "New Customers (7d)", "14", "+3 from previous week", "C", "success"));

        List<DashboardModels.ChartPoint> trend = List.of(
                new DashboardModels.ChartPoint("Mon", 9500),
                new DashboardModels.ChartPoint("Tue", 11200),
                new DashboardModels.ChartPoint("Wed", 10800),
                new DashboardModels.ChartPoint("Thu", 13450),
                new DashboardModels.ChartPoint("Fri", 14900),
                new DashboardModels.ChartPoint("Sat", 12500),
                new DashboardModels.ChartPoint("Sun", 16020)
        );

        List<DashboardModels.ChartPoint> topProducts = List.of(
                new DashboardModels.ChartPoint("Laptop", 340),
                new DashboardModels.ChartPoint("Office Chair", 280),
                new DashboardModels.ChartPoint("Printer", 190),
                new DashboardModels.ChartPoint("Monitor", 260),
                new DashboardModels.ChartPoint("Router", 170)
        );

        List<DashboardModels.RecentSale> sales = List.of(
                new DashboardModels.RecentSale("S-120", "Apex Retail", new BigDecimal("1240.00"), "Paid", LocalDate.now()),
                new DashboardModels.RecentSale("S-119", "Bright Foods", new BigDecimal("830.50"), "Unpaid", LocalDate.now().minusDays(1)),
                new DashboardModels.RecentSale("S-118", "Vision Mart", new BigDecimal("4130.00"), "Paid", LocalDate.now().minusDays(1)),
                new DashboardModels.RecentSale("S-117", "City Traders", new BigDecimal("690.00"), "Pending", LocalDate.now().minusDays(2)),
                new DashboardModels.RecentSale("S-116", "Next Point", new BigDecimal("1599.90"), "Paid", LocalDate.now().minusDays(2)),
                new DashboardModels.RecentSale("S-115", "Northline", new BigDecimal("2200.00"), "Unpaid", LocalDate.now().minusDays(3)),
                new DashboardModels.RecentSale("S-114", "Global Hub", new BigDecimal("499.00"), "Paid", LocalDate.now().minusDays(3)),
                new DashboardModels.RecentSale("S-113", "Prime Local", new BigDecimal("730.00"), "Paid", LocalDate.now().minusDays(4)),
                new DashboardModels.RecentSale("S-112", "Vertex", new BigDecimal("910.00"), "Pending", LocalDate.now().minusDays(4)),
                new DashboardModels.RecentSale("S-111", "Quick Shop", new BigDecimal("1230.75"), "Paid", LocalDate.now().minusDays(5))
        );

        List<DashboardModels.LowStockItem> lowStock = List.of(
                new DashboardModels.LowStockItem("Laser Toner", 2, 8, "SupplyOne", "Low"),
                new DashboardModels.LowStockItem("A4 Paper Box", 6, 12, "PaperMax", "Low"),
                new DashboardModels.LowStockItem("Wireless Mouse", 3, 10, "Tech Source", "Low"),
                new DashboardModels.LowStockItem("Desk Lamp", 1, 6, "Bright Ltd", "Critical"),
                new DashboardModels.LowStockItem("USB Hub", 4, 9, "ConnectPro", "Low")
        );

        List<DashboardModels.ActivityItem> activity = List.of(
                new DashboardModels.ActivityItem("10:24", "Sale #S-120 added"),
                new DashboardModels.ActivityItem("09:40", "Stock updated for Laser Toner"),
                new DashboardModels.ActivityItem("09:05", "Customer Apex Retail created"),
                new DashboardModels.ActivityItem("Yesterday", "Purchase order #P-33 approved"),
                new DashboardModels.ActivityItem("Yesterday", "Payment reminder sent to Bright Foods")
        );

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase(Locale.ROOT);
            sales = sales.stream().filter(s -> matchesSale(s, filterType, q)).collect(Collectors.toList());
            lowStock = lowStock.stream().filter(s -> s.product().toLowerCase(Locale.ROOT).contains(q)).collect(Collectors.toList());
        }

        int notifications = (int) lowStock.stream().filter(s -> "critical".equalsIgnoreCase(s.status())).count() + 7;

        return new DashboardModels.DashboardSnapshot(kpis, trend, topProducts, sales, lowStock, activity, notifications);
    }

    private boolean matchesSale(DashboardModels.RecentSale sale, String filterType, String query) {
        if ("Customers".equalsIgnoreCase(filterType)) {
            return sale.customer().toLowerCase(Locale.ROOT).contains(query);
        }
        if ("Invoices".equalsIgnoreCase(filterType)) {
            return sale.invoiceNo().toLowerCase(Locale.ROOT).contains(query);
        }
        if ("Products".equalsIgnoreCase(filterType)) {
            return false;
        }
        return sale.invoiceNo().toLowerCase(Locale.ROOT).contains(query)
                || sale.customer().toLowerCase(Locale.ROOT).contains(query)
                || sale.status().toLowerCase(Locale.ROOT).contains(query);
    }
}
