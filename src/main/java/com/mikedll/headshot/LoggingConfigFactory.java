package com.mikedll.headshot;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;

public class LoggingConfigFactory extends ConfigurationFactory {

    static Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
        // return new DefaultConfiguration();
        builder.setConfigurationName(name);
        builder.setStatusLevel(Level.ERROR);

        // builder.add(builder.newLogger("org.apache.hc.client5.http.headers", Level.DEBUG));
        // builder.add(builder.newLogger("org.apache.hc.client5.http", Level.DEBUG));


        // these don't do anything
        // builder.add(builder.newLogger("org.apache.catalina.core.StandardService", Level.ERROR));
        // builder.add(builder.newLogger("org.apache.coyote.AbstractProtocol", Level.ERROR));
        
        builder.add(builder.newLogger("com.mikedll.headshot.Application", Level.DEBUG));
    
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
