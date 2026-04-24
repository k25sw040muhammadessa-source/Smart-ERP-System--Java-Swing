package javaapplication1.erp.ui.screens.panels;

import javaapplication1.erp.service.AuthManager;
import javaapplication1.erp.ui.theme.Theme;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit logs panel with table view.
 */
public class AuditLogsPanel extends Panel {

    private TableCanvas tableCanvas;

    public AuditLogsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);

        Label headerLabel = new Label("Audit Logs");
        headerLabel.setFont(Theme.FONT_HEADING);
        headerLabel.setAlignment(Label.LEFT);
        headerLabel.setForeground(Theme.TEXT_PRIMARY);
        add(headerLabel, BorderLayout.NORTH);

        tableCanvas = new TableCanvas();
        add(tableCanvas, BorderLayout.CENTER);
    }

    public void refreshData() {
        tableCanvas.reloadData();
    }

    private static final class TableCanvas extends Canvas implements MouseWheelListener {

        private final List<String[]> data = new ArrayList<>();
        private final String[] headers = {"Time", "Action", "Actor", "Details"};
        private final int[] colWidths = {180, 180, 140, 460};
        private int scrollOffset = 0;
        private Image buffer;

        public TableCanvas() {
            setBackground(Theme.SURFACE);
            reloadData();
            addMouseWheelListener(this);
        }

        private void reloadData() {
            data.clear();
            java.util.List<AuthManager.AuditEntry> auditEntries = AuthManager.getAuditTrail();
            for (AuthManager.AuditEntry entry : auditEntries) {
                data.add(new String[]{entry.timestamp, entry.action, entry.actor, entry.details});
            }
            if (data.isEmpty()) {
                data.add(new String[]{"-", "SYSTEM", "app", "No audit records yet."});
            }
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            int width = Math.max(1, getWidth());
            int height = Math.max(1, getHeight());
            if (buffer == null || buffer.getWidth(this) != width || buffer.getHeight(this) != height) {
                buffer = createImage(width, height);
            }
            Graphics2D g2d = (Graphics2D) buffer.getGraphics();
            g2d.setColor(Theme.SURFACE);
            g2d.fillRect(0, 0, width, height);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setFont(Theme.FONT_BODY);
            FontMetrics fm = g2d.getFontMetrics();

            int rowHeight = Theme.TABLE_ROW_HEIGHT;
            int headerHeight = Theme.TABLE_HEADER_HEIGHT;
            int y = 0;
            int xStart = Theme.SPACING_16;

            g2d.setColor(new Color(0xF7F8FA));
            g2d.fillRoundRect(xStart, y, width - (Theme.SPACING_16 * 2), headerHeight, 8, 8);
            g2d.setColor(Theme.TEXT_PRIMARY);
            int x = xStart;
            for (int i = 0; i < headers.length; i++) {
                g2d.drawString(headers[i], x + Theme.SPACING_8, y + (headerHeight + fm.getAscent() - fm.getDescent()) / 2);
                x += colWidths[i];
            }
            y += headerHeight;

            int visibleHeight = height - headerHeight;
            int totalRowsHeight = data.size() * rowHeight;
            scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, totalRowsHeight - visibleHeight)));

            for (int row = 0; row < data.size(); row++) {
                int rowY = y + (row * rowHeight) - scrollOffset;
                if (rowY + rowHeight < headerHeight || rowY > height) {
                    continue;
                }
                if (row % 2 == 0) {
                    g2d.setColor(new Color(0xFCFCFD));
                    g2d.fillRect(xStart, rowY, width - (Theme.SPACING_16 * 2), rowHeight);
                }

                g2d.setColor(Theme.BORDER);
                g2d.drawLine(xStart, rowY, width - Theme.SPACING_16, rowY);

                g2d.setColor(Theme.TEXT_PRIMARY);
                String[] rowData = data.get(row);
                int rowX = xStart;
                for (int col = 0; col < rowData.length && col < headers.length; col++) {
                    String value = rowData[col];
                    int maxChars = Math.max(4, (colWidths[col] - 20) / 7);
                    if (value.length() > maxChars) {
                        value = value.substring(0, maxChars - 1) + "...";
                    }
                    g2d.drawString(value, rowX + Theme.SPACING_8, rowY + (rowHeight + fm.getAscent() - fm.getDescent()) / 2);
                    rowX += colWidths[col];
                }
            }

            g.drawImage(buffer, 0, 0, this);
            g2d.dispose();
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            scrollOffset += e.getWheelRotation() * 24;
            repaint();
        }
    }
}