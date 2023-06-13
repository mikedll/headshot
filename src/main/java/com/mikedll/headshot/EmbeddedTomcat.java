package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

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

public class EmbeddedTomcat {

    private int port = 8080;
    
    private final Tomcat tomcat;

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
        

        // Dynamic content - use our servlet
        Wrapper defaultServlet = ctx.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("com.mikedll.headshot.Servlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.setLoadOnStartup(1);
        ctx.addChild(defaultServlet);


        // Static content - use default servlet
        Wrapper staticServlet = ctx.createWrapper();
        staticServlet.setName("static");
        staticServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        staticServlet.addInitParameter("debug", "0");
        staticServlet.addInitParameter("listings", "false");
        staticServlet.setLoadOnStartup(1);
        ctx.addChild(staticServlet);

        ctx.addServletMappingDecoded("/", "default");
        ctx.addServletMappingDecoded("/static/*", "static");

        // Make context aware of our static resources
        File localStaticRoot = new File("web_assets");
        WebResourceRoot resources = new StandardRoot(ctx);
        DirResourceSet dirSet = new DirResourceSet(resources, "/static", localStaticRoot.getAbsolutePath(), "/");
        dirSet.setReadOnly(true);
        resources.addPreResources(dirSet);
        
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
