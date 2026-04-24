package javaapplication1.erp.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Utility for generating lightweight PDF reports.
 */
public final class PdfReportUtil {
    private PdfReportUtil() {
    }

    public static void generateTableReport(String outputPath, String title, String[] headers, List<String[]> rows)
            throws Exception {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            document.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100f);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setPadding(6f);
                table.addCell(cell);
            }

            for (String[] row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value));
                    cell.setPadding(5f);
                    table.addCell(cell);
                }
            }

            document.add(table);
        } catch (DocumentException ex) {
            throw new Exception("Failed to generate PDF report", ex);
        } finally {
            document.close();
        }
    }
}
