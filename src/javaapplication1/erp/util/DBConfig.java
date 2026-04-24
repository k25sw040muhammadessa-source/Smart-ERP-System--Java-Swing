package javaapplication1.erp.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * DBConfig loads DB properties from the classpath.
 */
public final class DBConfig {
    private static final Properties props = new Properties();
    private static final String[] CONFIG_PATHS = {
            "/db.properties",
            "/javaapplication1/resources/db.properties"
    };

    static {
        try (InputStream in = openConfigStream()) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("db.properties not found. Checked: /db.properties and /javaapplication1/resources/db.properties");
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConfig() {}

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String getUrl() {
        String host = safe("db.host", "127.0.0.1");
        String port = safe("db.port", "3306");
        String db = safe("db.name", "smart_erp");
        String params = get("db.params");
        if (params == null || params.trim().isEmpty()) {
            params = "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectionTimeZone=UTC&characterEncoding=UTF-8";
        }
        return String.format("jdbc:mysql://%s:%s/%s?%s", host, port, db, params);
    }

    private static String safe(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private static InputStream openConfigStream() {
        for (String path : CONFIG_PATHS) {
            InputStream in = DBConfig.class.getResourceAsStream(path);
            if (in != null) {
                return in;
            }
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            for (String path : CONFIG_PATHS) {
                String normalized = path.startsWith("/") ? path.substring(1) : path;
                InputStream in = cl.getResourceAsStream(normalized);
                if (in != null) {
                    return in;
                }
            }
        }

        return null;
    }
}
