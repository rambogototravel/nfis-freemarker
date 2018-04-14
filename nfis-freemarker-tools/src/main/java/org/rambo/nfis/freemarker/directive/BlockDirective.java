package org.rambo.nfis.freemarker.directive;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.util.Map;

/**
 * @author Rambo Yang
 * @date 2017/07/19
 */
public class BlockDirective implements TemplateDirectiveModel{

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if (params.isEmpty()) {
            throw new TemplateModelException("@block：This directive arguments missing.");
        }





    }

}
