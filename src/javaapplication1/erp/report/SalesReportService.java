package javaapplication1.erp.report;

import javaapplication1.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales reporting service for date range reports.
 */
public class SalesReportService {
    private static final String SALES_RANGE_SQL =
            "SELECT so.order_no, c.name AS customer_name, so.order_date, so.total_amount " +
            "FROM sales_orders so INNER JOIN customers c ON so.customer_id = c.id " +
            "WHERE so.order_date BETWEEN ? AND ? AND so.status <> 'CANCELLED' ORDER BY so.order_date ASC";

    public List<String[]> salesRows(LocalDate from, LocalDate to) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SALES_RANGE_SQL)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("order_no"),
                            rs.getString("customer_name"),
                            String.valueOf(rs.getDate("order_date")),
                            rs.getBigDecimal("total_amount").toPlainString()
                    });
                }
            }
        }
        return rows;
    }

    public void exportSalesRangePdf(LocalDate from, LocalDate to, String outputPath) throws Exception {
        String[] headers = {"Order No", "Customer", "Order Date", "Total Amount"};
        PdfReportUtil.generateTableReport(outputPath, "Sales Report (" + from + " to " + to + ")", headers, salesRows(from, to));
    }
}
