package com.mikedll.headshot;

import jakarta.servlet.http.HttpServletRequest;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.context.Context;
    
public class Controller {
    protected FileTemplateResolver templateResolver = new FileTemplateResolver();

    protected TemplateEngine templateEngine = new TemplateEngine();

    public Context defaultCtx(HttpServletRequest req) {
        Context ctx = new Context(req.getLocale());

        String snippet = "<!-- google analytics disabled -->";

        /*
        <!-- Google tag (gtag.js) -->
        <script async src="https://www.googletagmanager.com/gtag/js?id=ID"></script>
        <script>
          window.dataLayer = window.dataLayer || [];
          function gtag(){dataLayer.push(arguments);}
          gtag('js', new Date());

          gtag('config', 'ID');
        </script>
        */
        
        // adjust for prod
        ctx.setVariable("googleAnalytics", snippet);
        return ctx;
    }
    
    public Controller() {
        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("web_app_views/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
    }

}
