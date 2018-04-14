package org.rambo.nfis.freemarker.directive;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Rambo Yang
 */
public class BodyDirective implements TemplateDirectiveModel {
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        Writer out = env.getOut();

        //1
        /*if (params.isEmpty()) {
            out.write("<body>");
        }
        else {
            Iterator iterator = params.entrySet().iterator();
            StringBuffer buffer = new StringBuffer();
            buffer.append("<body");
            while (iterator.hasNext()) {
                Map.Entry<String, TemplateModel> entry = (Map.Entry<String, TemplateModel>) iterator.next();
                if (!"".equals(getString(entry.getValue()))) {
                    buffer.append(" " + entry.getKey() + "=" + getString(entry.getValue()));
                }
            }
            buffer.append(">");
            out.write(buffer.toString());
        }
        out.write("</body>");*/

        //2
        TemplateModel classz = (TemplateModel) params.get("class");
        if (classz instanceof TemplateSequenceModel) {
            TemplateSequenceModel sequenceModel = (TemplateSequenceModel) classz;
            if (sequenceModel.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("<body class=\"");
                for (int i = 0; i < sequenceModel.size(); i++) {
                    buffer.append(sequenceModel.get(i)).append(" ");
                }
                buffer.deleteCharAt(buffer.length() - 1);
                buffer.append("\"></body>");
                out.write(buffer.toString());
            }
        }

        body.render(out);
    }


    private String getString(TemplateModel templateModel) throws TemplateModelException {
        if (templateModel instanceof TemplateScalarModel) {
            TemplateScalarModel stringModel = (TemplateScalarModel) templateModel;
            return stringModel.getAsString();
        }
        else {
            return null;
        }
    }

}
