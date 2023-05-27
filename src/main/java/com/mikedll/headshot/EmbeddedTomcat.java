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

import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;

public class EmbeddedTomcat {

    private int port = 8080;
    
    private final Object monitor = new Object();

    private final Tomcat tomcat;

    private volatile boolean started;

    public EmbeddedTomcat() {
        this.tomcat = new Tomcat();
    }
    
    public void start() {
        try {
            this.tomcat.start();
            
            // Create a default connector.
            this.tomcat.getConnector();
            
            System.out.println("Tomcat started on port: " + port);
            this.tomcat.getServer().await();
        } catch(LifecycleException ex) {
            System.out.println("LifecycleException: " + ex.getMessage());
        }
    }

    private ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = EmbeddedTomcat.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }
    
    public void prepare() {
        tomcat.setPort(port);

        File baseDir = createTempDir("tomcat");
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        StandardContext ctx = new StandardContext();
        ctx.setPath("");
        ctx.setName("Default Context");
        ctx.addLifecycleListener(new FixContextListener());

        // Setup class loading
        ClassLoader parentClassLoader = getDefaultClassLoader();
        WebappLoader loader = new WebappLoader();
        loader.setLoaderInstance(new TomcatEmbeddedWebappClassLoader(parentClassLoader));
        loader.setDelegate(true);
        ctx.setLoader(loader);
        
        Wrapper defaultServlet = ctx.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("com.mikedll.headshot.Servlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        // Otherwise the default location of a Spring DispatcherServlet cannot be set
        defaultServlet.setOverridable(true);

        ctx.addChild(defaultServlet);
        ctx.addServletMappingDecoded("/", "default");

        this.tomcat.getHost().addChild(ctx);
    }

    private class WebServerException extends RuntimeException {
        public WebServerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
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
            throw new WebServerException(
                                         "Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
        }
    }
    
}
