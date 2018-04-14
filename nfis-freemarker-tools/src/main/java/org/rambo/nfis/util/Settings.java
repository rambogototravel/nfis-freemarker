package org.rambo.nfis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rambo Yang
 */
public class Settings {
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    protected static final String KEY = ServletContext.class.getName();

    /** nfis配置默认路径 */
    public static String DEFAULT_PATH = "/WEB-INF/properties/nfis.properties";

    /** nfis 配置属性数据 */
    protected static Properties data = new Properties();

    /** map表数据结构 */
    protected static Map<String, Object> map = new HashMap<>();

    /**
     *
     * @param context
     */
    public static void init(ServletContext context) {
        if (getApplicationAttribute(KEY) != null) {
            return;
        }

        setApplicationAttribute(KEY, context);
        load(context.getResourceAsStream(DEFAULT_PATH));
    }

    /**
     *
     * @param key
     * @param value
     */
    public static void setApplicationAttribute(String key, Object value) {
        map.put(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public static Object getApplicationAttribute(String key) {
        return map.get(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public static String getValue(String key) {
        return data.getProperty(key);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getValue(String key, String defaultValue) {
        String value = getValue(key);
        if (value == null || "".equals(value)) {
            return defaultValue;
        }

        return value;
    }

    /**
     *
     * @param stream
     */
    public static void load(InputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            data.load(stream);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }


    public static void reload() {
        if (getApplicationAttribute(KEY) == null) {
            return;
        }

        ServletContext context = (ServletContext) getApplicationAttribute(KEY);
        load(context.getResourceAsStream(DEFAULT_PATH));
    }

    /**
     * 获取map表的实际目录路径
     * @return
     */
    public static String getMapDir() {
        String mapDir = Settings.getValue("mapDir", "/WEB-INF/config");
        String mapDirType = Settings.getValue("mapDirType", "webapp");

        if ("webapp".equals(mapDirType)) {
            ServletContext sc = (ServletContext) Settings.getApplicationAttribute(KEY);

            if (!mapDir.startsWith("/")) {
                mapDir = "/" + mapDir;
            }

            mapDir = sc.getRealPath(mapDir);
        }

        return mapDir;
    }
}
