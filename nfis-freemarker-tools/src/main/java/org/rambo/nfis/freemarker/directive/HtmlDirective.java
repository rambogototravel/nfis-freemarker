package org.rambo.nfis.freemarker.directive;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Rambo Yang
 */
public class HtmlDirective implements TemplateDirectiveModel {
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        StringBuffer buffer = new StringBuffer();

        if (params.isEmpty()) {
            buffer.append("<html></html>");
        }
        else {
            buffer.append("<html ");

            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = getString(params.get(key));
                if (isNotBlank(value)) {
                    buffer.append(key).append("=").append(value).append(" ");
                }
            }

            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append("></html>");
        }

        Writer out = env.getOut();
        out.write(buffer.toString());
        body.render(out);
    }

    private boolean isBlank(String str) {
        return str == null || "".equals(str);
    }

    private boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    private String getString(Object obj) throws TemplateModelException {
        if (obj instanceof TemplateScalarModel) {
            return ((TemplateScalarModel) obj).getAsString();
        }
        else {
            return null;
        }
    }

}
