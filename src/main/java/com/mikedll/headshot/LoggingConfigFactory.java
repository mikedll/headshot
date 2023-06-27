package com.mikedll.headshot;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
    
public class LoggingConfigFactory extends ConfigurationFactory {

    private Config config;
    
    public LoggingConfigFactory(Config config) {
        this.config = config;
    }
    
    private Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
        builder.setConfigurationName(name);
        builder.setStatusLevel(Level.ERROR);

        // We don't mess with the root logger. We set additivity to false to avoid
        // inheriting things from it (or if we don't set that to false we get double
        // logging).
        
        AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
        LayoutComponentBuilder standardLayout = builder.newLayout("PatternLayout");
        standardLayout.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
        console.add(standardLayout);
        builder.add(console);

        AppenderComponentBuilder sqlConsole = builder.newAppender("sqlconsole", "Console");
        LayoutComponentBuilder sqlLayout = builder.newLayout("PatternLayout");
        sqlLayout.addAttribute("pattern", "SQL %-5level: %msg%n");
        sqlConsole.add(sqlLayout);
        builder.add(sqlConsole);        

        // Turn on verbose logging in rest client calls.
        // builder.add(builder.newLogger("org.apache.hc.client5.http.headers", Level.DEBUG));
        // builder.add(builder.newLogger("org.apache.hc.client5.http", Level.DEBUG));


        // Trying to shutup tomcat but these don't do anything. I hear tomcat uses the JUL, whatever that is.
        // builder.add(builder.newLogger("org.apache.catalina.core.StandardService", Level.ERROR));
        // builder.add(builder.newLogger("org.apache.coyote.AbstractProtocol", Level.ERROR));

        LoggerComponentBuilder appLogger = builder.newLogger("com.mikedll.headshot.Application");
        appLogger.addAttribute("additivity", false);
        appLogger.add(builder.newAppenderRef("stdout"));
        if(config.env == "development") {
            appLogger.addAttribute("level", Level.DEBUG);
        } else if(config.env == "test") {
            appLogger.addAttribute("level", Level.FATAL);
        } else {
            appLogger.addAttribute("level", Level.INFO);
        }
        builder.add(appLogger);

        LoggerComponentBuilder dbLogger = builder.newLogger("com.mikedll.headshot.db.DatabaseConfiguration");
        dbLogger.addAttribute("additivity", false);
        dbLogger.add(builder.newAppenderRef("sqlconsole"));
        if(config.env == "test") {
            dbLogger.addAttribute("level", Level.FATAL);
        } else {
            dbLogger.addAttribute("level", Level.DEBUG);
        }
        builder.add(dbLogger);
    
        return builder.build();
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
        return getConfiguration(loggerContext, source.toString(), null);
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final String name, final URI configLocation) {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        return createConfiguration(name, builder);
    }

    @Override
    protected String[] getSupportedTypes() {
        return new String[] {"*"};
    }    
}
