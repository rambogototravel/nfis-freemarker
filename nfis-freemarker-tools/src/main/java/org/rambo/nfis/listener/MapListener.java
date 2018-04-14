package org.rambo.nfis.listener;

import org.rambo.nfis.util.MapCache;
import org.rambo.nfis.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Method;

/**
 * map监听器，ServletContextListener实现类。
 * @author Rambo Yang
 */
public class MapListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(MapListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        Settings.init(sc); //初始化nfis配置

        initMapCache(sc); //初始化MapCahce，加载map表数据
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}

    /**
     * 初始化MapCache
     * @param servletContext
     */
    private void initMapCache(ServletContext servletContext) {
        String mapCacheClassName = Settings.getValue("mapCacheClass");

        MapCache mc;
        if (mapCacheClassName == null) {
            mc = MapCache.getInstance();
            mc.init(servletContext);
        }
        else {
            try {
                Class mapCacheClass = Class.forName(mapCacheClassName);
                Method instMethod = mapCacheClass.getMethod("getInstance");
                Object inst = instMethod.invoke(null, null);

                if (inst instanceof MapCache) {
                    MapCache instance = (MapCache) inst;
                    MapCache.setInstance(instance);

                    mc = MapCache.getInstance();
                    mc.init(servletContext);

                    logger.info(String.format("MapCache load succeed[&s].", mc.getClass()));
                }
                else {
                    throw new Exception(String.format("Failed to convert object [%s] to MapCache.", inst.getClass()));
                }

            } catch (Exception e) {
                logger.error("MapCache load failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
