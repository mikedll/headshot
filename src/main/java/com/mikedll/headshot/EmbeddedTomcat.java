package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.naming.ContextBindings;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.Host;
import org.apache.catalina.startup.Tomcat.FixContextListener;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.javatuples.Pair;

public class EmbeddedTomcat {

    private int port = 8080;
    
    private final Tomcat tomcat;

    public static final String PUBLIC_ROOT_DIR = "/static";

    public static final String PUBLIC_FONT_DIR = "/webfonts";
    
    public EmbeddedTomcat() {
        this.tomcat = new Tomcat();
    }
    
    public void start() {
        // Create a default connector.
        this.tomcat.getConnector();
            
        System.out.println("Tomcat started on port: " + port);
        this.tomcat.getServer().await();
    }
    
    public void prepare() {            
        tomcat.setPort(port);

        File baseDir = createTempDir("tomcat");
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        StandardContext ctx = new StandardContext();
        ctx.setPath("");
        ctx.setName("Default Context");
        ctx.addLifecycleListener(new FixContextListener());
        ctx.setParentClassLoader(EmbeddedTomcat.class.getClassLoader());
        

        // General content - use our servlet
        Wrapper defaultServlet = ctx.createWrapper();
        defaultServlet.setName("defaultServlet");
        defaultServlet.setServletClass("com.mikedll.headshot.controller.Servlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.setLoadOnStartup(1);
        ctx.addChild(defaultServlet);


        // Static content under /static - use default servlet
        Wrapper staticServlet = ctx.createWrapper();
        staticServlet.setName("staticServlet");
        staticServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        staticServlet.addInitParameter("debug", "0");
        staticServlet.addInitParameter("listings", "false");
        staticServlet.setLoadOnStartup(1);
        ctx.addChild(staticServlet);

        ctx.addServletMappingDecoded("/", "defaultServlet");
        ctx.addServletMappingDecoded(PUBLIC_ROOT_DIR + "/*", "staticServlet");
        ctx.addServletMappingDecoded(PUBLIC_FONT_DIR + "/*", "staticServlet");

        // Make context aware of our static resources
        WebResourceRoot resources = new StandardRoot(ctx);

        List<Pair<String,String>> staticDirs = new ArrayList<>(2);
        staticDirs.add(Pair.with(PUBLIC_ROOT_DIR, "web_assets"));
        staticDirs.add(Pair.with(PUBLIC_FONT_DIR, "node_modules/@fortawesome/fontawesome-free/webfonts"));

        staticDirs.forEach(staticDir -> {
                DirResourceSet dirSet = new DirResourceSet(resources, staticDir.getValue0(),
                                                           new File(staticDir.getValue1()).getAbsolutePath(), "/");
                dirSet.setReadOnly(true);
                resources.addPreResources(dirSet);
            });
        
        ctx.setResources(resources);

        this.tomcat.getHost().addChild(ctx);

        try {
            tomcat.start();        
        } catch(LifecycleException ex) {
            System.out.println("LifecycleException: " + ex.getMessage());
        }
    }
    
    /**
     * From org.springframework.boot.web.server.AbstractConfigurableWebServerFactory
     * 
     * Return the absolute temp dir for given web server.
     * @param prefix server name
     * @return the temp dir for given server.
     */
    private final File createTempDir(String prefix) {
        try {
            File tempDir = Files.createTempDirectory(prefix + "." + this.port + ".").toFile();
            tempDir.deleteOnExit();
            return tempDir;
        }
        catch (IOException ex) {
            throw new RuntimeException("Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
        }
    }
    
}
