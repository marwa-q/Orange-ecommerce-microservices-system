package com.orange.notification_service.mail;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
public class MailTemplateRenderer {

    private final TemplateEngine templateEngine;

    public MailTemplateRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Render a template from mail-templates/<template>.html using provided model.
     * TemplateEngine is thread-safe once configured as a bean.
     */
    public String render(String templateName, Map<String, Object> model) {
        Context context = new Context();
        if (model != null) context.setVariables(model);
        return templateEngine.process(templateName, context);
    }
}
