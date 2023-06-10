package com.mikedll.headshot;

import java.lang.ClassNotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.io.IOException;
import java.util.LinkedHashSet;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.core.annotation.AnnotationFilter;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.ReflectionUtils;

import com.mikedll.headshot.controller.Request;

public class Experiment2 {

    public void run() {
        System.out.println("Hello");

        ClassLoader classLoader = this.getClass().getClassLoader();
        List<Properties> result = new ArrayList<>();

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String path = "classpath*:com/mikedll/headshot/controller/**/*.class";

        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        try {
            Resource[] resources = resolver.getResources(path);
            for(Resource resource : resources) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                /*
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                String name = classMetadata.getClassName();
                System.out.println(name);

                Class clazz;
                try {
                    clazz = Class.forName(name);
                } catch (ClassNotFoundException ex) {
                    System.out.println("ClassNotFoundException from " + name + ": " + ex.getMessage());
                    continue;
                }
                */

                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                // System.out.println("annotationMetadata: " + annotationMetadata + " of class " + annotationMetadata.getClass());
                // StandardAnnotationMetadata annotationMetadata = new StandardAnnotationMetadata(clazz);
                

                Set<MethodMetadata> methods = new LinkedHashSet<>();
                annotationMetadata.getAnnotatedMethods("com.mikedll.headshot.controller.Request").forEach(m -> {
                        System.out.println(m);
                    });
                

            }
        } catch(IOException ex) {
            System.out.println("IOException when scanning for controllers: " + ex.getMessage());
        }
        // Set<String> types = new HashSet<>();

        result.forEach(p -> System.out.println(p));
    }
}
