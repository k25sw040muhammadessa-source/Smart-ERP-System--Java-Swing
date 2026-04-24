package javaapplication1.erp.ui.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs used by the dashboard UI.
 */
public final class DashboardModels {

    private DashboardModels() {
    }

    public record KpiMetric(String id, String label, String value, String trendText, String iconText, String tone) {}

    public record ChartPoint(String label, double value) {}

    public record RecentSale(String invoiceNo, String customer, BigDecimal amount, String status, LocalDate date) {}

    public record LowStockItem(String product, int stock, int reorderLevel, String supplier, String status) {}

    public record ActivityItem(String timestamp, String message) {}

    public record DashboardSnapshot(
            List<KpiMetric> kpis,
            List<ChartPoint> salesTrend,
            List<ChartPoint> topProducts,
            List<RecentSale> recentSales,
            List<LowStockItem> lowStock,
            List<ActivityItem> activities,
            int pendingNotifications
    ) {}
}
