package com.mikedll.headshot;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
    
public class AbstractController {
    protected ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

    protected TemplateEngine templateEngine = new TemplateEngine();
    
    public AbstractController() {
        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
    }

}
