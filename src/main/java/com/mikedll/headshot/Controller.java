package com.mikedll.headshot;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.UnsupportedEncodingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Controller {
    protected FileTemplateResolver templateResolver = new FileTemplateResolver();

    protected TemplateEngine templateEngine = new TemplateEngine();

    protected Map<String,Object> session = null;

    private final String cookieName = "HEADSHOT_SESSION";

    protected CookieManager cookieManager = new CookieManager(Env.cookieSigningKey);

    public Controller() {
        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("web_app_views/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
    }
    
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

    /*
     * Returns false if request service should be aborted.
     */
    public boolean beforeFilters(boolean requireValidCookie, HttpServletRequest req, HttpServletResponse res) {
        boolean cookieCheckOkay = true;
        for(Cookie cookie : req.getCookies()) {
            if(cookie.getName().equals(cookieName)) {
                CookieManager.VerifyResult result = null;
                try {
                    result = cookieManager.verify(cookie.getValue());
                } catch (UnsupportedEncodingException ex) {
                    System.out.println("UnsupportedEncodingException when verifing cookie: " + ex.getMessage());
                    sendInternalServerError(res, "Internal logic error: unsupported encoding when parsing cookie");
                    return false;
                } catch (JsonProcessingException ex) {
                    System.out.println("JsonProcessingException when verifing cookie: " + ex.getMessage());
                    if(requireValidCookie) {
                        cookieCheckOkay = false;
                    }
                }
                
                if(result.ok()) {
                    this.session = result.deserialized();
                } else if(requireValidCookie == false) {
                    // pass
                } else {
                    cookieCheckOkay = false;
                }
            } else {
                this.session = new LinkedHashMap<String,Object>();
            }
        }

        if(!cookieCheckOkay) {
            System.out.println("beforeFilters is redirecting to root");
            sendRedirect(res, localOrigin(req) + "/");
            return false;
        }            

        return true;
    }

    public void flushCookies(HttpServletResponse res) {
        try {
            Cookie sessionCookie = new Cookie(cookieName, cookieManager.cookieString(this.session));
            sessionCookie.setAttribute("SameSite", "Lax");
            sessionCookie.setAttribute("Path", "/");            
            res.addCookie(sessionCookie);
        } catch (JsonProcessingException ex) {
            throw new RequestException("Unable calculate cookie", ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RequestException("Unable to flush cookies", ex);
        }
    }

    public String localOrigin(HttpServletRequest req) {
        String scheme = "http://";
        String fullHost = req.getServerName();
        int port = req.getServerPort();
        if(port != 80) {
            fullHost += ":" + port;
        }
        // System.out.println("localOrigin: " + scheme + fullHost);
        return scheme + fullHost;
    }

    public void sendInternalServerError(HttpServletResponse res, String message) {
        try {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        } catch (IOException ex) {
            throw new RuntimeException("failed to render 500", ex);
        }
    }

    public void render(String template, Context ctx, HttpServletResponse res) {
        try {
            templateEngine.process(template, ctx, res.getWriter());
        } catch (IOException ex) {
            throw new RuntimeException("failed to render template: " + template, ex);
        }
    }

    public void sendRedirect(HttpServletResponse res, String path) {
        try {
            res.sendRedirect(path);
        } catch (IOException ex) {
            throw new RequestException("IOException when redirecting to: " + path + ", " + ex.getMessage(), ex);
        }
    }
}
