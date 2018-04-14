package org.rambo.nfis.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rambo Yang
 */
public class ServletUtils {

    /**
     *
     * @param req
     * @return
     */
    public static String getPath(HttpServletRequest req) {
        String path = (String) req.getAttribute("javax.servlet.include.servlet_path");
        String info = (String) req.getAttribute("javax.servlet.include.path_info");
        if (path == null) {
            path = req.getServletPath();
            info = req.getPathInfo();
        }

        if (info != null) {
            path = path + info;
        }

        return path;
    }


}
