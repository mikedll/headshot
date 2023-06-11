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

import java.lang.ClassNotFoundException;

import java.lang.reflect.Constructor;

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
        String path = "classpath*:com/mikedll/headshot/experiment/**/*.class";

        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        try {
            Resource[] resources = resolver.getResources(path);
            for(Resource resource : resources) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                AnnotationMetadata classMetadata = metadataReader.getAnnotationMetadata();
                
                System.out.println("class: " + classMetadata.getClassName());
                Set<MethodMetadata> methods = classMetadata.getAnnotatedMethods("com.mikedll.headshot.experiment.Tacky");
                for(MethodMetadata method : methods) {
                    System.out.println("annotated method: " + method + " of class " + method.getClass());
                    System.out.println("  method: " + method.getMethodName());
                }

                if(methods.size() > 0) {
                    Class clazz = null;
                    try {
                        clazz = Class.forName(classMetadata.getClassName());
                    } catch (ClassNotFoundException ex) {
                        System.out.println("ClassNotFoundException for " + classMetadata.getClassName() + ": " + ex.getMessage());
                    }
                    if(clazz != null) {
                        handleAnnotations(clazz);
                    }
                }            
            }
        } catch(IOException ex) {
            System.out.println("IOException when scanning for controllers: " + ex.getMessage());
        }
        // Set<String> types = new HashSet<>();

        result.forEach(p -> System.out.println(p));
    }

    public void handleAnnotations(Class clazz) {
        Constructor<?>[] candidates = clazz.getDeclaredConstructors();
        Constructor<?> ctorToUse = null;
        for(Constructor<?> candidate : candidates) {
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if(parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == Integer.class) {
                System.out.println("Found matching constructor");
                ctorToUse = candidate;
            }
        }

        if(ctorToUse != null) {
            System.out.println("Found constructor: " + ctorToUse);
            Object created = null;
            try {
                created = ctorToUse.newInstance(new Object[] { "Minny", 15 });
            } catch (Throwable ex) {
                System.out.println("Exception when instantiating " + clazz + ": " + ex.getMessage());
            }
            System.out.println("created: " + created); 
        }
                
        // method.invoke(bean, arguments);
        // constructorToUse = clazz.getDeclaredConstructor();
        
    }
}
