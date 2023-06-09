package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Optional;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.javatuples.Pair;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mikedll.headshot.apiclients.GithubClient;
import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.Application;
import com.mikedll.headshot.Config;
import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.AssetFingerprinter;
import com.mikedll.headshot.util.JsonMarshal;

public class Controller {
    public static final String COOKIE_NAME = "HEADSHOT_SESSION";

    public static final String CONTENT_TYPE = "Content-Type";
    
    public static final String CONTENT_TYPE_JSON = "application/json";

    public Application app;
    
    private TemplateEngine templateEngine;
    
    private UserRepository baseUserRepository;

    private boolean cookieFiltersOkay = false;

    private boolean authOkay = false;
    
    private boolean rendered = false;

    private boolean baseDbAccess = false;

    private AssetFingerprinter assetFingerprinter;
    
    protected HttpServletRequest req;
    
    protected HttpServletResponse res;

    protected boolean requireAuthentication = true;

    protected CookieManager cookieManager;

    protected Logger logger;

    protected User currentUser;

    protected Map<String,Object> session = null;

    protected PathMatch pathMatch;

    public void setRequest(HttpServletRequest req) {
        this.req = req;
    }

    public void setResponse(HttpServletResponse res) {
        this.res = res;
    }

    public void setApplication(Application app) {
        this.app = app;
        this.cookieManager= new CookieManager(this.app.config.cookieSigningKey);
        this.assetFingerprinter = this.app.assetFingerprinter;
        this.templateEngine = this.app.templateEngine;
        this.logger = this.app.logger;
    }

    public <T> T getRepository(Class<T> clazz) {
        return this.app.dbConf.getRepository(this, clazz);
    }

    public GithubClient getGithubClient(String accessToken) {
        return this.app.apiClientManager.getGithubClient(this, accessToken);
    }

    public void setPathMatch(PathMatch pathMatch) {
        this.pathMatch = pathMatch;
    }

    public String getPathParam(String key) {
        return this.pathMatch.extractedParams().get(key);
    }

    public Config getConfig() {
        return this.app.config;
    }

    public ObjectMapper getJsonObjectMapper() {
        return this.app.jsonObjectMapper;
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
        ctx.setVariable("assets", this.assetFingerprinter.getForViewsWithoutLock());

        return ctx;
    }

    public void clearSession() {
        this.session = new HashMap<String,Object>();
    }

    public boolean canAccessData() {
        return this.baseDbAccess || (this.authOkay && this.cookieFiltersOkay);
    }
    
    /*
     * Returns false if service of the given request should be aborted.
     */
    public boolean cookieFilters() {
        boolean cookieCheckOkay = true;

        if(req.getCookies() == null) {
            this.cookieFiltersOkay = true;
            return this.cookieFiltersOkay;
        }

        Cookie cookie = Arrays.asList(req.getCookies()).stream()
            .filter(c -> c.getName().equals(COOKIE_NAME)).findAny().orElse(null);
        
        if(cookie != null) {
            Pair<Map<String,Object>,String> result = cookieManager.verify(getJsonObjectMapper(), cookie.getValue());
            if(result.getValue1() != null) {
                cookieCheckOkay = false;
            } else {
                this.session = result.getValue0();
            }
        } else {
            this.session = new HashMap<String,Object>();
        }

        if(!cookieCheckOkay) {
            this.logger.error("cookieFilters is resetting cookies redirecting to root");
            clearSession();
            sendCookies();
            sendRedirect("/");
            return false;
        }

        this.cookieFiltersOkay = true;        
        return this.cookieFiltersOkay;
    }

    /*
     * Returns false if request service should be aborted.
     */ 
    public boolean authFilters() {
        if(this.session != null && session.get("user_id") != null) {
            this.baseDbAccess = true;
            this.baseUserRepository = getRepository(UserRepository.class);
            this.baseDbAccess = false;
            Pair<Optional<User>, String> userFetch = baseUserRepository.findById(((Integer)session.get("user_id")).longValue());
            if(userFetch.getValue1() != null) {
                sendInternalServerError(userFetch.getValue1());
                return false;
            }
            this.currentUser = userFetch.getValue0().orElse(null);
        }

        this.authOkay = (!this.requireAuthentication || this.currentUser != null);

        if(!this.authOkay) {
            clearSession();
            sendCookies();
            sendRedirect("/");
        }

        return this.authOkay;
    }
    
    public boolean prepare() {        
        if(!cookieFilters()) return false;

        declareAuthRequirements();
        if(!authFilters()) return false;
        
        acquireDataAccess();

        return true;
    }

    /*
     * For subclasses to override and request db access.
     */
    public void acquireDataAccess() {
    }

    /*
     * For subclasses to override.
     */
    public void declareAuthRequirements() {
    }

    public void sendCookies() {
        Pair<String,String> strResult = cookieManager.cookieString(getJsonObjectMapper(), this.session);
        if(strResult.getValue1() != null) {
            throw new RuntimeException("Error: " + strResult.getValue1());
        }
        Cookie sessionCookie = new Cookie(COOKIE_NAME, strResult.getValue0());
        sessionCookie.setAttribute("SameSite", "Lax");
        sessionCookie.setAttribute("Path", "/");
        res.addCookie(sessionCookie);
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
        sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    public void sendNotFound() {
        sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
    }
    
    public void sendError(int status, String message) {
        if(this.rendered) {
            throw new RequestException("response already sent");
        }

        try {
            if(req.getHeader("Accept").equals(CONTENT_TYPE_JSON)) {
                Map<String,String> response = new HashMap<>();
                response.put("error", message);
                Pair<String,String> marshalled = JsonMarshal.marshal(getJsonObjectMapper(), response);
                if(marshalled.getValue1() != null) {
                    res.sendError(status, message);
                } else {
                    sendJson(marshalled.getValue0(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                res.sendError(status, message);
            }
            String st = String.join("\n", Arrays.asList(Thread.currentThread().getStackTrace())
                                    .stream().map(ste -> ste.toString()).collect(Collectors.toList()));
            this.logger.error("Rendering " + status + " error: " + message + ", Stacktrace follows:\n" + st);
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
            res.setStatus(HttpServletResponse.SC_OK);
            templateEngine.process(template, ctx, res.getWriter());
        } catch (IOException ex) {
            throw new RuntimeException("failed to render template: " + template, ex);
        }
        this.rendered = true;
    }

    public void sendRedirect(String path) {
        sendRedirectWorld(localOrigin() + path);
    }

    public void sendJson(String json, int status) {
        res.setStatus(status);
        res.setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        try {
            res.getWriter().write(json);
        } catch (IOException ex) {
            throw new RuntimeException("failed to write JSON", ex);
        }
    }
    
    public void sendJson(String json) {
        sendJson(json, HttpServletResponse.SC_OK);
    }
    
    public void sendRedirectWorld(String path) {
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

    public static void setupTemplateEngine() {
    }        
    
}
