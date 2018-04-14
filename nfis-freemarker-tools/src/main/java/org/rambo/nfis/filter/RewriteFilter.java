package org.rambo.nfis.filter;

import com.alibaba.fastjson.JSONObject;
import org.rambo.nfis.util.MapCache;
import org.rambo.nfis.util.Settings;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 重写/重定向规则过滤器
 * @author Rambo Yang
 */
public class RewriteFilter implements Filter {

    private MapCache map = null;

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        /* 开发环境，让修改及时生效，反正真正的后端是不会执行到这的！*/
        Settings.reload();
        map = MapCache.getInstance();

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        //先执行 rewrite
        if (false) {
            return;
        }
        else if (handlePreview(request, response)) {
            return;
        }

        chain.doFilter(req, resp);
    }

    public void destroy() {}

    protected Boolean handlePreview(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = req.getServletPath();
        URL url = req.getSession().getServletContext().getResource(path);

        if (url == null) {
            Pattern reg = Pattern.compile("^/(?:([^/]+)/)?(.*)$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = reg.matcher(path);

            if (matcher.find()) {
                String ns = matcher.group(1);
                String file = matcher.group(2);

                JSONObject info = null;
                String fisId = null;
                String[] tryFiles = Settings.getValue("tryFiles", ",.html,.ftl,.jsp").split(",");

                for (String tryFile:tryFiles) {

                    info = ns != null ? map.getNode( (fisId = ns + "/" + file + tryFile)) : null;
                    if (info != null) {
                        break;
                    }

                    info = ns != null ? map.getNode( (fisId = ns + ":" + file + tryFile)) : map.getNode((fisId = file + tryFile));
                    if (info != null) {
                        break;
                    }
                }

                // 在 map.json 里面找到了
                if (info!=null) {
                    req.setAttribute("requestFISID", fisId);
                    String resolved = info.getString("uri");

                    if (resolved.endsWith(".jsp")) {
                        resolved = Settings.getValue("jspDir", "/WEB-INF/template") + resolved;
                    } else {
                        resolved = Settings.getValue("views.path", "/WEB-INF/template") + resolved;
                    }

                    req.getRequestDispatcher(resolved).forward(req, resp);
                    return true;
                } else {
                    return false;
                }

            }
        }
        else if (path.endsWith(".json")) {
            resp.addHeader("Content-Type", "application/json");
        }

        return false;
    }

    /**
     * 读取server.conf配置规则，进行转发
     * @param request
     * @param response
     * @return
     */
    protected Boolean handleRewrite(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // 查找重定向规则配置文件
        List<String> ruleConfFiles = new ArrayList<>();
        Set<String> files = request.getServletContext().getResourcePaths(RewriteRulers.DEFAULT_DIR);
        if (files != null) {
            for (String file : files) {
                if (file.endsWith(".conf") && file.contains("/server")) {
                    ruleConfFiles.add(file);
                }
            }
        }

        // 添加、排序重定向配置文件路径列表
        final List<String> orders = new ArrayList<>();
        orders.add(RewriteRulers.DEFAULT_DIR + "servercommon.conf");
        orders.add(RewriteRulers.DEFAULT_PATH);
        Collections.sort(ruleConfFiles, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return orders.indexOf(o2) - orders.indexOf(o1);
            }
        });

        /*
         * 加载重写/重定向规则
         */
        RewriteRulers parser = new RewriteRulers();
        for (String path : ruleConfFiles) {
            InputStream stream = request.getServletContext().getResourceAsStream(path);
            if (stream != null) {
                parser.load(stream);
            }
        }

        /*
         * 根据访问路径，获取对应的规则，并跳转
         */
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI().substring(contextPath.length());
        RewriteRulers.Ruler ruler = parser.getRuler(requestURI);
        if (ruler != null) {
            if (ruler.type == RewriteRulers.Ruler.TYPE_REDIRECT) {
                response.sendRedirect(contextPath + ruler.dest);
            }
            else if (ruler.type == RewriteRulers.Ruler.TYPE_REWRITE) {
                request.getRequestDispatcher(ruler.dest).forward(request, response);
            }

            return true;
        }

        return false;
    }

    /**
     * 重定向规则类
     */
    protected static class RewriteRulers {
        /** 默认目录 */
        protected static final String DEFAULT_DIR = "/WEB-INF/";

        /** 重定向规则配置默认路径 */
        protected static final String DEFAULT_PATH = "/WEB-INF/config/server.conf";

        protected static class Ruler {
            public static final int TYPE_REWRITE = 0;
            public static final int TYPE_REDIRECT = 1;

            /** 跳转类型，默认0（重写） */
            public int type = 0;

            /** 匹配模式 */
            public String pattern;

            /** 目标路径 */
            public String target;

            /** 目标路径 */
            public String dest;

            @Override
            public String toString() {
                return "Ruler{" +
                        "type=" + type +
                        ", pattern='" + pattern + '\'' +
                        ", target='" + target + '\'' +
                        ", dest='" + dest + '\'' +
                        '}';
            }
        }

        protected ArrayList<Ruler> rulers = new ArrayList<Ruler>();

        public RewriteRulers() {}

        public RewriteRulers(InputStream stream) throws IOException {
            this.load(stream, Charset.forName("UTF-8"));
        }

        public RewriteRulers(InputStream stream, Charset charset) throws IOException {
            this.load(stream, charset);
        }

        /**
         * 加载重定向规则
         * @param stream
         * @throws IOException
         */
        public void load(InputStream stream) throws IOException {
            this.load(stream, Charset.forName("UTF-8"));
        }

        /**
         * 加载重定向规则
         * @param stream
         * @param charset
         * @throws IOException
         */
        protected void load(InputStream stream, Charset charset) throws IOException {
            InputStreamReader instream = new InputStreamReader(stream, charset);
            BufferedReader reader = new BufferedReader(instream);

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                //只识别 rewrite/redirect
                if (line.isEmpty() || !line.startsWith("rewrite") && !line.startsWith("redirect")) {
                    continue;
                }

                /*
                 * 空格分隔，每行规则为Ruler对象
                 */
                String[] parts = line.split("\\s+");
                Ruler ruler = new Ruler();
                if ("rewrite".equals(parts[0].toLowerCase())) {
                    ruler.type = Ruler.TYPE_REWRITE;
                }
                else if ("redirect".equals(parts[0].toLowerCase())) {
                    ruler.type = Ruler.TYPE_REDIRECT;
                }

                ruler.pattern = parts[1];
                ruler.target = parts[2];

                rulers.add(ruler);
            }

            reader.close();
            instream.close();
        }

        /**
         * 获取重写规则
         * @param path
         * @return
         */
        protected Ruler getRuler(String path) {
            for (Ruler ruler : rulers) {
                if (path.matches(ruler.pattern)) {
                    ruler.dest = path.replaceAll(ruler.pattern, ruler.target);
                    return ruler;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return "RewriteRulers{" +
                    "rulers=" + rulers +
                    '}';
        }
    }
}
