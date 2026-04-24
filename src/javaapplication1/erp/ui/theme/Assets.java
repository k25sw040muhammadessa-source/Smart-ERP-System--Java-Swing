package javaapplication1.erp.ui.theme;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Asset loading utilities for icons and images.
 * Loads icons from resources/icons/ folder.
 */
public class Assets {

    private static final Map<String, BufferedImage> iconCache = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> scaledCache = new ConcurrentHashMap<>();

    public static BufferedImage getIcon(String name) {
        String key = name.toLowerCase();
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }

        BufferedImage fromResource = loadResourceIcon(key);
        if (fromResource != null) {
            iconCache.put(key, fromResource);
            return fromResource;
        }

        BufferedImage fallback = createFallbackIcon(key, Theme.NAV_ICON_SIZE);
        iconCache.put(key, fallback);
        return fallback;
    }

    public static BufferedImage getAppLogo() {
        return getIcon("logo");
    }

    public static BufferedImage getIconScaled(String name, int size) {
        String key = name.toLowerCase() + "#" + size;
        if (scaledCache.containsKey(key)) {
            return scaledCache.get(key);
        }
        BufferedImage base = getIcon(name);
        if (base == null) return null;
        BufferedImage scaled = getScaledInstance(base, size, size, true);
        scaledCache.put(key, scaled);
        return scaled;
    }

    private static BufferedImage loadResourceIcon(String name) {
        String path = "/icons/" + name + ".png";
        URL url = Assets.class.getResource(path);
        if (url != null) {
            try {
                BufferedImage img = ImageIO.read(url);
                if (img != null && "logo".equals(name)) {
                    int w = img.getWidth();
                    int h = img.getHeight();
                    // Crop to the upper square region to remove any embedded text under the mark.
                    int cropSize = Math.min(w, (int) (h * 0.72));
                    int cropX = Math.max(0, (w - cropSize) / 2);
                    int cropY = Math.max(0, (int) (h * 0.04));
                    if (cropY + cropSize > h) {
                        cropY = Math.max(0, h - cropSize);
                    }
                    BufferedImage cropped = new BufferedImage(cropSize, cropSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = cropped.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.drawImage(img, -cropX, -cropY, null);
                    g.dispose();
                    return cropped;
                }
                return img;
            } catch (IOException ignored) {
                // fall through to filesystem fallback
            }
        }

        // Development fallback: try loading directly from project folder (src/icons)
        File f = new File("src/icons/" + name + ".png");
        if (f.exists()) {
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null && "logo".equals(name)) {
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int cropSize = Math.min(w, (int) (h * 0.72));
                    int cropX = Math.max(0, (w - cropSize) / 2);
                    int cropY = Math.max(0, (int) (h * 0.04));
                    if (cropY + cropSize > h) {
                        cropY = Math.max(0, h - cropSize);
                    }
                    BufferedImage cropped = new BufferedImage(cropSize, cropSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = cropped.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.drawImage(img, -cropX, -cropY, null);
                    g.dispose();
                    return cropped;
                }
                return img;
            } catch (IOException ignored) {
                return null;
            }
        }

        return null;
    }

    private static BufferedImage createFallbackIcon(String name, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(
            Theme.PRIMARY_BLUE.getRed(),
            Theme.PRIMARY_BLUE.getGreen(),
            Theme.PRIMARY_BLUE.getBlue(),
            235
        ));
        g2d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Minimal line-icon placeholders, coherent style across all entries.
        if (name.contains("product")) {
            g2d.draw(new RoundRectangle2D.Double(3, 4, size - 6, size - 8, 4, 4));
            g2d.drawLine(3, size / 2, size - 3, size / 2);
        } else if (name.contains("customer") || name.contains("employee") || name.contains("supplier")) {
            g2d.drawOval(size / 2 - 3, 3, 6, 6);
            g2d.drawRoundRect(4, 10, size - 8, size - 13, 6, 6);
        } else if (name.contains("inventory") || name.contains("stock")) {
            g2d.drawRect(3, 5, size - 6, size - 10);
            g2d.drawLine(3, size / 2, size - 3, size / 2);
            g2d.drawLine(size / 2, 5, size / 2, size - 5);
        } else if (name.contains("sales") || name.contains("purchase")) {
            g2d.drawLine(4, size - 5, size - 4, 5);
            g2d.drawLine(size - 4, 5, size - 8, 5);
            g2d.drawLine(size - 4, 5, size - 4, 9);
        } else if (name.contains("payment") || name.contains("ledger")) {
            g2d.drawRoundRect(3, 6, size - 6, size - 12, 6, 6);
            g2d.drawLine(6, 10, size - 6, 10);
        } else if (name.contains("unit")) {
            g2d.drawRect(4, 4, size - 8, size - 8);
            g2d.drawLine(4, 4, size - 4, size - 4);
        } else if (name.contains("permission")) {
            g2d.drawOval(5, 4, size - 10, size - 10);
            g2d.drawLine(size / 2, size / 2, size / 2, size - 3);
        } else if (name.contains("audit")) {
            g2d.drawOval(4, 4, size - 8, size - 8);
            g2d.drawLine(size / 2, size / 2, size - 4, size - 4);
        } else if (name.contains("logo")) {
            g2d.setColor(Theme.PRIMARY_BLUE);
            g2d.fillRoundRect(0, 0, size, size, 7, 7);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(11, size / 2)));
            g2d.drawString("S", size / 2 - 4, size / 2 + 5);
        } else {
            g2d.drawRoundRect(3, 3, size - 6, size - 6, 5, 5);
        }
        g2d.dispose();
        return img;
    }

    private static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, boolean highQuality) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w = img.getWidth();
        int h = img.getHeight();

        if (highQuality) {
            BufferedImage tmp = img;
            while (w > targetWidth || h > targetHeight) {
                w = Math.max(targetWidth, w / 2);
                h = Math.max(targetHeight, h / 2);
                BufferedImage next = new BufferedImage(w, h, type);
                Graphics2D g2 = next.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(tmp, 0, 0, w, h, null);
                g2.dispose();
                tmp = next;
            }
            if (tmp.getWidth() != targetWidth || tmp.getHeight() != targetHeight) {
                BufferedImage finalImg = new BufferedImage(targetWidth, targetHeight, type);
                Graphics2D g2 = finalImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(tmp, 0, 0, targetWidth, targetHeight, null);
                g2.dispose();
                ret = finalImg;
            } else {
                ret = tmp;
            }
        } else {
            BufferedImage finalImg = new BufferedImage(targetWidth, targetHeight, type);
            Graphics2D g2 = finalImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
            g2.dispose();
            ret = finalImg;
        }

        return ret;
    }
}