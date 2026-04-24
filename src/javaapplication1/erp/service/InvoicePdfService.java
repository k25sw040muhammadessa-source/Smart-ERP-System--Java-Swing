package javaapplication1.erp.service;

import javaapplication1.erp.util.DBUtil;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Exports sales invoice PDFs.
 */
public class InvoicePdfService {
    public String buildSalesInvoicePreview(int orderId) throws Exception {
        StringBuilder sb = new StringBuilder();
        String headerSql = "SELECT so.order_no, so.order_date, c.name customer_name, so.total_amount " +
                "FROM sales_orders so JOIN customers c ON c.id = so.customer_id WHERE so.id = ?";
        String linesSql = "SELECT soi.product_id, p.name, soi.qty, soi.unit_price, soi.line_total " +
                "FROM sales_order_items soi JOIN products p ON p.id = soi.product_id WHERE soi.sales_order_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement hs = connection.prepareStatement(headerSql);
             PreparedStatement ls = connection.prepareStatement(linesSql)) {
            hs.setInt(1, orderId);
            ls.setInt(1, orderId);
            try (ResultSet hr = hs.executeQuery(); ResultSet lr = ls.executeQuery()) {
                if (!hr.next()) {
                    throw new IllegalArgumentException("Sales order not found: " + orderId);
                }
                sb.append("Invoice: ").append(hr.getString("order_no").replace("SO-", "INV-")).append("\n");
                sb.append("Order No: ").append(hr.getString("order_no")).append("\n");
                sb.append("Date: ").append(hr.getDate("order_date")).append("\n");
                sb.append("Customer: ").append(hr.getString("customer_name")).append("\n\n");
                sb.append("Items:\n");
                while (lr.next()) {
                    sb.append("- ").append(lr.getString("name"))
                            .append(" (ID ").append(lr.getInt("product_id")).append(")")
                            .append(" | Qty: ").append(lr.getInt("qty"))
                            .append(" | Unit: ").append(lr.getBigDecimal("unit_price"))
                            .append(" | Line: ").append(lr.getBigDecimal("line_total"))
                            .append("\n");
                }
                sb.append("\nGrand Total: ").append(hr.getBigDecimal("total_amount"));
            }
        }
        return sb.toString();
    }

    public void exportSalesInvoice(int orderId, String outputPath) throws Exception {
        String headerSql = "SELECT so.order_no, so.order_date, c.name customer_name, so.total_amount " +
                "FROM sales_orders so JOIN customers c ON c.id = so.customer_id WHERE so.id = ?";
        String linesSql = "SELECT soi.product_id, p.name, soi.qty, soi.unit_price, soi.line_total " +
                "FROM sales_order_items soi JOIN products p ON p.id = soi.product_id WHERE soi.sales_order_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement hs = connection.prepareStatement(headerSql);
             PreparedStatement ls = connection.prepareStatement(linesSql)) {
            hs.setInt(1, orderId);
            ls.setInt(1, orderId);
            try (ResultSet hr = hs.executeQuery(); ResultSet lr = ls.executeQuery()) {
                if (!hr.next()) {
                    throw new IllegalArgumentException("Sales order not found: " + orderId);
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(outputPath));
                document.open();
                document.add(new Paragraph("Smart ERP - Sales Invoice",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Invoice No: " + hr.getString("order_no").replace("SO-", "INV-")));
                document.add(new Paragraph("Order No: " + hr.getString("order_no")));
                document.add(new Paragraph("Date: " + hr.getDate("order_date")));
                document.add(new Paragraph("Customer: " + hr.getString("customer_name")));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100f);
                addHead(table, "Product ID");
                addHead(table, "Product");
                addHead(table, "Qty");
                addHead(table, "Unit Price");
                addHead(table, "Line Total");
                while (lr.next()) {
                    table.addCell(String.valueOf(lr.getInt("product_id")));
                    table.addCell(lr.getString("name"));
                    table.addCell(String.valueOf(lr.getInt("qty")));
                    table.addCell(lr.getBigDecimal("unit_price").toPlainString());
                    table.addCell(lr.getBigDecimal("line_total").toPlainString());
                }
                document.add(table);
                document.add(new Paragraph(" "));
                BigDecimal total = hr.getBigDecimal("total_amount");
                document.add(new Paragraph("Grand Total: " + total.toPlainString(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.close();
            }
        }
    }

    private void addHead(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setPadding(6f);
        table.addCell(cell);
    }
}
