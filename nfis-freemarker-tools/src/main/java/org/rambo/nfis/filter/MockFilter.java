package org.rambo.nfis.filter;

import com.alibaba.fastjson.JSONObject;
import org.rambo.nfis.util.Settings;
import org.rambo.nfis.util.UnicodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 绑定模拟数据过滤器
 * @author Rambo Yang
 */
public class MockFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(MockFilter.class);

    private static final String key = MockFilter.class.getName();

    private static final String reg = "(^/|/$|\\..+$)";

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        if (req.getAttribute(key) == null) {
            req.setAttribute(key, true);

            HttpServletRequest request = (HttpServletRequest) req;
            this.attachJson(request);
        }

        chain.doFilter(req, resp);
    }

    public void destroy() {}

    /**
     * 添加JSON数据，根据请问路径到/test目录下读取对应的JSON数据
     * @param request
     */
    protected void attachJson(HttpServletRequest request) {
        List<String> tryPaths = new ArrayList<>();
        tryPaths.add("global");

        //FIXME??
        String path = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (path != null) {
            if (!request.getContextPath().isEmpty()) {
                path = path.substring(request.getContextPath().length());
            }
            tryPaths.add(path.replaceAll(reg, ""));
        }

        path = (String) request.getAttribute("requestFISID");
        if (path != null) {
            tryPaths.add(path.replace(":", "/").replaceAll(reg, ""));
        }

        /*
         * 去除“/”、“.”的前后缀
         */
        path = request.getServletPath();
        if (path.startsWith(Settings.getValue("template.path", "/WEB-INF/template"))) {
            path = path.substring(Settings.getValue("template.path", "/WEB-INF/template").length());
            tryPaths.add(path.replaceAll(reg, ""));
        }
        else {
            tryPaths.add(path.replaceAll(reg, ""));
        }

        JSONObject jsonData = new JSONObject();

        /*
         * 遍历访问路径下的所有json文件，添加为JSON数据结构
         */
        for (String path2 : tryPaths) {
            if (path2.isEmpty()) {
                continue;
            }

            String[] parts = path2.split("/+"); //以/斜线分隔
            String prefix = "/test";
            String suffix = ".json";

            for (String part : parts) {
                String jsonPath = prefix + "/" + part + suffix;

                try {
                    URL url = request.getServletContext().getResource(jsonPath);
                    if (url != null) {
                        String encoding = Settings.getValue("encoding", "UTF-8");
                        BufferedReader in = new BufferedReader(new UnicodeReader(url.openStream(), encoding));

                        StringBuilder builder = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            builder.append(inputLine);
                        }
                        in.close();

                        JSONObject targetData = JSONObject.parseObject(builder.toString());
                        this.extendJson(jsonData, targetData);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }

                prefix += "/" + part;
            }
        }

        request.setAttribute("data", jsonData);
    }

    /**
     * 扩展JSON数据
     * @param source
     * @param target
     */
    private void extendJson(JSONObject source, JSONObject target) {
        if (source == null || target == null) {
            return;
        }

        for (String key : target.keySet()) {
            Object value = target.get(key);
            if (source.containsKey(key) && source.get(key) instanceof JSONObject && value instanceof JSONObject) {
                this.extendJson(source.getJSONObject(key), target.getJSONObject(key));
            }
            else {
                source.put(key, value);
            }
        }
    }
}
