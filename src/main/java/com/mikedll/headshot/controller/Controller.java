package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.context.Context;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.mikedll.headshot.UserRepository;
import com.mikedll.headshot.User;
import com.mikedll.headshot.CookieManager;
import com.mikedll.headshot.Env;
import com.mikedll.headshot.Application;

public class Controller {
    private FileTemplateResolver templateResolver = new FileTemplateResolver();

    private TemplateEngine templateEngine = new TemplateEngine();

    private final String cookieName = "HEADSHOT_SESSION";

    private UserRepository baseUserRepository;

    private boolean cookieFiltersOkay = false;

    private boolean authOkay = false;
    
    private boolean rendered = false;

    private boolean baseDbAccess = false;
    
    protected HttpServletRequest req;
    
    protected HttpServletResponse res;

    protected boolean requireAuthentication = true;

    protected CookieManager cookieManager = new CookieManager(Env.cookieSigningKey);

    protected User currentUser;

    protected Map<String,Object> session = null;    
    
    public Controller() {
        this.req = req;
        this.res = res;
        
        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("web_app_views/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(Env.env == "production");

        templateEngine.addDialect(new LayoutDialect());
        templateEngine.setTemplateResolver(templateResolver);
    }

    public void setRequest(HttpServletRequest req) {
        this.req = req;
    }

    public void setResponse(HttpServletResponse res) {
        this.res = res;
    }
    
    public Context defaultCtx() {
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
        ctx.setVariable("currentUser", currentUser);

        return ctx;
    }

    public void clearSession() {
        this.session = new LinkedHashMap<String,Object>();
    }

    public boolean canAccessDb() {
        return this.baseDbAccess || (this.authOkay && this.cookieFiltersOkay);
    }
    
    /*
     * Returns false if service of the given request should be aborted.
     */
    public boolean cookieFilters() {
        boolean cookieCheckOkay = true;
        for(Cookie cookie : req.getCookies()) {
            if(cookie.getName().equals(cookieName)) {
                CookieManager.VerifyResult result = null;
                try {
                    result = cookieManager.verify(cookie.getValue());
                } catch (UnsupportedEncodingException ex) {
                    System.out.println("UnsupportedEncodingException when verifing cookie: " + ex.getMessage());
                    sendInternalServerError("Internal logic error: unsupported encoding when parsing cookie");
                    return false;
                } catch (JsonProcessingException ex) {
                    System.out.println("JsonProcessingException when verifing cookie: " + ex.getMessage());
                    cookieCheckOkay = false;
                }
                
                if(result.ok()) {
                    this.session = result.deserialized();
                } else {
                    // We found a cookie by our name but the sig test failed. This is a hack attempt.
                    cookieCheckOkay = false;
                }
            } else {
                this.session = new LinkedHashMap<String,Object>();
            }
        }

        if(!cookieCheckOkay) {
            System.out.println("cookieFilters is resetting cookies redirecting to root");
            clearSession();
            sendCookies();
            sendRedirect(localOrigin() + "/");
            return false;
        }

        this.cookieFiltersOkay = true;        
        return this.cookieFiltersOkay;
    }

    public boolean authFilters() {
        if(session.get("user_id") != null) {
            this.baseDbAccess = true;
            this.baseUserRepository = Application.dbConf.getRepository(this, UserRepository.class);
            this.baseDbAccess = false;
            Optional<User> user = baseUserRepository.findById(((Integer)session.get("user_id")).longValue());
            if(user.isPresent()) this.currentUser = user.get();
        }

        this.authOkay = (!this.requireAuthentication || this.currentUser != null);

        if(!this.authOkay) {
            clearSession();
            sendCookies();
            sendRedirect(localOrigin() + "/");
        }
        
        return this.authOkay;
    }
    
    public boolean prepare() {        
        if(!cookieFilters()) return false;

        declareAuthRequirements();
        if(!authFilters()) return false;
        
        acquireDbAccess();
        return true;
    }

    /*
     * For subclasses to override and request db access.
     */
    public void acquireDbAccess() {
    }

    /*
     * For subclasses to override.
     */
    public void declareAuthRequirements() {
    }

    public void sendCookies() {
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

    public String localOrigin() {
        String scheme = "http://";
        String fullHost = req.getServerName();
        int port = req.getServerPort();
        if(port != 80) {
            fullHost += ":" + port;
        }
        // System.out.println("localOrigin: " + scheme + fullHost);
        return scheme + fullHost;
    }

    public void sendInternalServerError(String message) {
        if(this.rendered) {
            throw new RequestException("response already sent");
        }
        
        try {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        } catch (IOException ex) {
            throw new RuntimeException("failed to send 500", ex);
        }
        this.rendered = true;
    }

    public void render(String template, Context ctx) {
        if(this.rendered) {
            throw new RequestException("response already sent");
        }
        
        try {
            templateEngine.process(template, ctx, res.getWriter());
        } catch (IOException ex) {
            throw new RuntimeException("failed to render template: " + template, ex);
        }
        this.rendered = true;
    }

    public void sendRedirect(String path) {
        if(this.rendered) {
            throw new RequestException("response already sent");
        }
        
        try {
            res.sendRedirect(path);
        } catch (IOException ex) {
            throw new RequestException("IOException when redirecting to: " + path + ", " + ex.getMessage(), ex);
        }
        this.rendered = true;
    }
}
