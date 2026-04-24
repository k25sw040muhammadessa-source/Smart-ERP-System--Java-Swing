package javaapplication1.erp.ui.screens;

import javaapplication1.erp.ui.theme.Assets;
import javaapplication1.erp.ui.theme.Theme;
import javaapplication1.erp.util.WindowUtil;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Splash screen frame with progress indicator.
 * Shows for ~2 seconds then opens login.
 */
public class SplashScreenFrame extends Frame {

    private float progress = 0.0f;
    private java.util.Timer timer;
    private double animationPhase = 0.0;
    private Runnable onComplete;
    private Image buffer;

    // Premium Dark Theme Colors
    private final Color BG_TOP = new Color(0x0F172A);      // Slate 900
    private final Color BG_BOTTOM = new Color(0x020617);   // Slate 950
    private final Color ACCENT_GLOW = new Color(0x3B, 0x82, 0xF6, 40); // Blue glow
    private final Color ACCENT = new Color(0x3B82F6);      // Blue 500
    private final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private final Color TEXT_MUTED = new Color(0x94A3B8);
    private final Color PROGRESS_BG = new Color(0x1E293B); // Slate 800

    public SplashScreenFrame(Runnable onComplete) {
        this.onComplete = onComplete;

        setUndecorated(true);
        setSize(500, 340);
        WindowUtil.centerWindow(this);
        // Set window icon to app logo (replaces default Java coffee icon)
        try {
            setIconImage(Assets.getIconScaled("logo", 64));
        } catch (Throwable ignored) {}
        setBackground(new Color(0, 0, 0, 0));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (timer != null) {
                    timer.cancel();
                }
                System.exit(0);
            }
        });

        timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                // Slower progress for a more leisurely splash
                progress += 0.01f;
                // Advance animation phase for pulsing
                animationPhase += 0.15;
                if (progress >= 1.0f) {
                    progress = 1.0f;
                    timer.cancel();
                    EventQueue.invokeLater(() -> {
                        dispose();
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                }
                repaint();
            }
        }, 0, 50); // slower tick for longer, smoother splash

        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        if (buffer == null || buffer.getWidth(this) != width || buffer.getHeight(this) != height) {
            buffer = createImage(width, height);
        }

        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // 1. Draw Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(8, 12, width - 16, height - 20, 24, 24);

        // 2. Main Background Gradient
        Shape mainArea = new java.awt.geom.RoundRectangle2D.Float(10, 10, width - 20, height - 20, 20, 20);
        GradientPaint bgGradient = new GradientPaint(0, 0, BG_TOP, 0, height, BG_BOTTOM);
        g2d.setPaint(bgGradient);
        g2d.fill(mainArea);
        
        // Outline border
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.setStroke(new BasicStroke(1f));
        g2d.draw(mainArea);

        // 3. Logo with Glow (reduced size, pulsing)
        int logoSize = Math.min(180, (int) (width * 0.45));
        int logoX = (width - logoSize) / 2;
        int logoY = 50;
        Image logo = Assets.getIconScaled("logo", logoSize);

        // Pulsing animation: small scale oscillation
        double pulse = 1.0 + Math.sin(animationPhase) * 0.04; // ~4% pulse
        int scaledLogoSize = Math.max(1, (int) (logoSize * pulse));
        int scaledLogoX = (width - scaledLogoSize) / 2;
        int scaledLogoY = logoY - (scaledLogoSize - logoSize) / 2;

        // Glow effect behind logo (smaller circle)
        g2d.setColor(ACCENT_GLOW);
        int glowPad = Math.max(6, scaledLogoSize / 10);
        g2d.fillOval(scaledLogoX - glowPad, scaledLogoY - glowPad, scaledLogoSize + (glowPad * 2), scaledLogoSize + (glowPad * 2));

        if (logo != null) {
            g2d.drawImage(logo, scaledLogoX, scaledLogoY, scaledLogoSize, scaledLogoSize, this);
        }

        // Subtitle / Loading text (centered below logo)
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(TEXT_MUTED);
        String loading = progress < 0.3 ? "Initializing modules..." : 
                 (progress < 0.7 ? "Connecting to database..." : "Loading your workspace...");
        int loadingY = scaledLogoY + scaledLogoSize + 36;
        g2d.drawString(loading, (width - g2d.getFontMetrics().stringWidth(loading)) / 2, loadingY);

        // 5. Custom Progress Bar (with moving blue knob)
        int barWidth = 280;
        int barHeight = 6;
        int barX = (width - barWidth) / 2;
        int barY = loadingY + 24; // position under the loading text, moved down

        // Track
        g2d.setColor(PROGRESS_BG);
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);

        // Fill gradient (represents actual progress)
        int fillWidth = Math.max(barHeight, (int) (barWidth * progress));
        GradientPaint progressGradient = new GradientPaint(barX, 0, new Color(0x60A5FA), barX + fillWidth, 0, new Color(0x2563EB));
        g2d.setPaint(progressGradient);
        g2d.fillRoundRect(barX, barY, fillWidth, barHeight, barHeight, barHeight);

        // Glow on progress (subtle)
        if (fillWidth > 10) {
            g2d.setColor(new Color(0x3B, 0x82, 0xF6, 100)); // faint glow
            g2d.fillRoundRect(barX, barY - 2, fillWidth, barHeight + 4, barHeight + 4, barHeight + 4);
        }

        // Animated knob that bounces left<->right
        int knobWidth = Math.max(14, barHeight * 5);
        int knobHeight = barHeight + 6;
        double sliderProgress = 0.5 * (1.0 + Math.sin(animationPhase * 0.5)); // bounce 0..1
        int knobX = barX + (int) ((barWidth - knobWidth) * sliderProgress);
        int knobY = barY - ((knobHeight - barHeight) / 2);

        GradientPaint knobGradient = new GradientPaint(knobX, 0, new Color(0xA7D2FF), knobX + knobWidth, 0, new Color(0x3B82F6));
        g2d.setPaint(knobGradient);
        g2d.fillRoundRect(knobX, knobY, knobWidth, knobHeight, knobHeight, knobHeight);

        // Small knob highlight
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillRoundRect(knobX + 2, knobY + 2, knobWidth - 6, knobHeight / 2, (knobHeight / 2), (knobHeight / 2));
        
        // Version info
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String version = "v2.0 Premium Edition";
        g2d.drawString(version, (width - g2d.getFontMetrics().stringWidth(version)) / 2, height - 25);

        g.drawImage(buffer, 0, 0, this);
        g2d.dispose();
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }
}