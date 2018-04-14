package org.rambo.nfis.spring;

import org.rambo.nfis.util.Settings;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * @author Rambo Yang
 */
public class NFisBean implements ServletContextAware {
    @Override
    public void setServletContext(ServletContext servletContext) {
        Settings.setApplicationAttribute(ServletContext.class.getName(), servletContext);
        Settings.load(servletContext.getResourceAsStream(Settings.DEFAULT_PATH));
    }
}
