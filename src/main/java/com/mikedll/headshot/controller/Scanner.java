package com.mikedll.headshot.controller;

import java.lang.ClassNotFoundException;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.annotation.MergedAnnotations;
import org.javatuples.Pair;

public class Scanner {

    public static final String ANNOTATION = "com.mikedll.headshot.controller.Request";

    private final String scanPath = "classpath*:com/mikedll/headshot/controller/**/*.class";

    public Pair<List<RequestHandler>, String> scan() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        Resource[] resources = null;
        try {
            resources = resolver.getResources(scanPath);
        } catch(IOException ex) {
            return Pair.with(null, "IOException when scanning for controllers: " + ex.getMessage());
        }
        
        List<RequestHandler> requestHandlers = new ArrayList<>(200);
        for(Resource resource : resources) {
            MetadataReader metadataReader = null;
            try {
                metadataReader = metadataReaderFactory.getMetadataReader(resource);
            } catch(IOException ex) {
                return Pair.with(null, "IOException when retrieving metadata reader: " + ex.getMessage());
            }
            AnnotationMetadata classMetadata = metadataReader.getAnnotationMetadata();

            Set<MethodMetadata> methods = classMetadata.getAnnotatedMethods(ANNOTATION);
            Class clazz = null;
            for(MethodMetadata method : methods) {
                if(clazz == null) {
                    try {
                        clazz = Class.forName(classMetadata.getClassName());
                    } catch (ClassNotFoundException ex) {
                        return Pair.with(null, "ClassNotFoundException for " + classMetadata.getClassName() + ": " + ex.getMessage());
                    }
                }
                Pair<RequestHandler, String> built = RequestHandler.build(clazz, method);
                if(built.getValue1() != null) {
                    return Pair.with(null, "Error when building handler for method " + method + ": " + built.getValue1());
                }
                requestHandlers.add(built.getValue0());
            }
        }

        return Pair.with(requestHandlers, null);
    }
}
