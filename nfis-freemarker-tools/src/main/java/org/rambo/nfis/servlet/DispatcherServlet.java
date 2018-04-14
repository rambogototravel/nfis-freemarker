package org.rambo.nfis.servlet;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.rambo.nfis.freemarker.directive.BodyDirective;
import org.rambo.nfis.freemarker.directive.RepeatDirective;
import org.rambo.nfis.freemarker.directive.UpperDirective;
import org.rambo.nfis.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * Servlet请求分配器
 * @author Rambo Yang
 */
public class DispatcherServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private Configuration configuration;

    private Properties properties = new Properties();

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void init() throws ServletException {
        if (this.configuration == null) {
            try {
                this.configuration = createConfiguration();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf8");

        String path = ServletUtils.getPath(req);

        JSONObject jsonData = (JSONObject) req.getAttribute("data");
        if (jsonData == null) {
            jsonData = new JSONObject();
        }

        try {
            processDispatchResult(path, jsonData, req, resp);
        } catch (TemplateException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 处理结果
     * @param url
     * @param model
     * @param req
     * @param resp
     * @throws IOException
     * @throws TemplateException
     */
    private void processDispatchResult(String url, Object model, HttpServletRequest req, HttpServletResponse resp) throws IOException, TemplateException {
        doRender(url, model, req, resp);
    }

    /**
     * 渲染模板
     * @param url
     * @param model
     * @param req
     * @param resp
     * @throws IOException
     * @throws TemplateException
     */
    private void doRender(String url, Object model, HttpServletRequest req, HttpServletResponse resp) throws IOException, TemplateException {
        processTemplate(getTemplate(url), model, req, resp);
    }

    /**
     * 获取模板
     * @param name
     * @return
     * @throws IOException
     */
    private Template getTemplate(String name) throws IOException {
        return getConfiguration().getTemplate(name);
    }

    /**
     * 处理模板
     * @param template
     * @param model
     * @param req
     * @param resp
     * @throws IOException
     * @throws TemplateException
     */
    private void processTemplate(Template template, Object model, HttpServletRequest req, HttpServletResponse resp) throws IOException, TemplateException {
        template.process(model, resp.getWriter());
    }

    /**
     * 创建Configuration 配置对象
     * @return
     * @throws IOException
     */
    protected Configuration createConfiguration() throws IOException {
        Configuration configuration = newConfiguation();

        configuration.setServletContextForTemplateLoading(getServletContext(), "/WEB-INF/template");
        configuration.setDefaultEncoding("UTF-8");


        /*configuration.setSharedVariable("upper", new UpperDirective());
        configuration.setSharedVariable("body", new BodyDirective());
        configuration.setSharedVariable("repeat", new RepeatDirective());*/

        try {
            ClassPathResource clsPathLoader = new ClassPathResource("properties/freemarker.properties");
            properties.load(clsPathLoader.getInputStream());
            configuration.setSettings(properties);
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    private Configuration newConfiguation() {
        return new Configuration(Configuration.VERSION_2_3_23);
    }
}
