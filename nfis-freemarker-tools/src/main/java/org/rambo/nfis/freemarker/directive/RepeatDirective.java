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
public class RepeatDirective implements TemplateDirectiveModel {

    private static final String PARAM_NAME_COUNT = "count";
    private static final String PARAM_NAME_HR = "hr";

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        // 处理参数:
        int countParam = 0;
        boolean countParamSet = false;
        boolean hrParam = false;

        Iterator paramIter = params.entrySet().iterator();
        while (paramIter.hasNext()) {
            Map.Entry ent = (Map.Entry) paramIter.next();
            String paramName = (String) ent.getKey();
            TemplateModel paramValue = (TemplateModel) ent.getValue();

            if (paramName.equals(PARAM_NAME_COUNT)) {
                if (!(paramValue instanceof TemplateNumberModel)) {
                    throw new TemplateModelException("The \"" + PARAM_NAME_HR + "\" parameter " + "must be a number.");
                }
                countParam = ((TemplateNumberModel) paramValue).getAsNumber().intValue();
                countParamSet = true;
                if (countParam < 0) {
                    throw new TemplateModelException("The \"" + PARAM_NAME_HR + "\" parameter " + "can't be negative.");
                }
            }
            else if (paramName.equals(PARAM_NAME_HR)) {
                if (!(paramValue instanceof TemplateBooleanModel)) {
                    throw new TemplateModelException("The \"" + PARAM_NAME_HR + "\" parameter " + "must be a boolean.");
                }
                hrParam = ((TemplateBooleanModel) paramValue).getAsBoolean();
            } else {
                throw new TemplateModelException(
                        "Unsupported parameter: " + paramName);
            }

            if (!countParamSet) {
                throw new TemplateModelException("The required \"" + PARAM_NAME_COUNT + "\" paramter" + "is missing.");
            }
            if (loopVars.length > 1) {
                throw new TemplateModelException("At most one loop variable is allowed.");
            }
            // 是啊, 它很长而且很无聊...
            // 执行真正指令的执行部分:
            Writer out = env.getOut();
            if (body != null) {
                for (int i = 0; i < countParam; i++) {
                    // 如果"hr"参数为真，那么就在所有重复部分之间打印<hr>:
                    if (hrParam && i != 0) {
                        out.write("<hr>");
                    }
                    // 如果有循环变量，那么就设置它:
                    if (loopVars.length > 0) {
                        loopVars[0] = new SimpleNumber(i + 1);
                    }
                    // 执行嵌入体部分（和 FTL 中的<#nested>一样）。
                    // 这种情况下，我们不提供一个特殊的 writer 作为参数:
                    body.render(env.getOut());
                }

            }
        }
    }
}
