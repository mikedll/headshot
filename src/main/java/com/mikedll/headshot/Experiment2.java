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
import java.util.function.Supplier;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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

import org.javatuples.Pair;

import com.mikedll.headshot.controller.Request;

public class Experiment2 {

    public void run() {
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
                MethodMetadata firstMethod = null;
                for(MethodMetadata method : methods) {
                    if(firstMethod == null) {
                        firstMethod = method;
                    }
                }

                if(methods.size() > 0) {
                    Class clazz = null;
                    try {
                        clazz = Class.forName(classMetadata.getClassName());
                    } catch (ClassNotFoundException ex) {
                        System.out.println("ClassNotFoundException for " + classMetadata.getClassName() + ": " + ex.getMessage());
                    }
                    if(clazz != null) {
                        Pair<Supplier<Pair<String,String>>, String> toRun = handleAnnotations(clazz, firstMethod);
                        if(toRun.getValue1() != null) {
                            System.out.println("Error when handling method: " + toRun.getValue1());
                            continue;
                        }

                        Pair<String,String> result = toRun.getValue0().get();
                        if(result.getValue1() != null) {
                            System.out.println("Error when running supplier: " + result.getValue1());
                        } else {
                            System.out.println("Ran supplier and got: " + result.getValue0());
                        }
                    }
                }            
            }
        } catch(IOException ex) {
            System.out.println("IOException when scanning for controllers: " + ex.getMessage());
        }
    }

    public Pair<Supplier<Pair<String,String>>, String> handleAnnotations(Class clazz, MethodMetadata methodMetadata) {
        Constructor<?>[] candidates = clazz.getDeclaredConstructors();
        Constructor<?> ctorToUse = null;
        for(Constructor<?> candidate : candidates) {
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if(parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == Integer.class) {
                ctorToUse = candidate;
                break;
            }
        }

        Object targetObject = null;
        if(ctorToUse != null) {
            try {
                targetObject = ctorToUse.newInstance(new Object[] { "Minny", 15 });
            } catch (Throwable ex) {
                return new Pair<>(null, "Exception when instantiating " + clazz + ": " + ex.getMessage());
            }
        } else {
            return new Pair<>(null, "found no suitable constructor");
        }

        Method[] methods = clazz.getMethods();
        Method annotatedMethod = null;
        for(Method candidate : methods) {
            if(candidate.getName().equals(methodMetadata.getMethodName())) {
                annotatedMethod = candidate;
                break;
            }
        }

        if(annotatedMethod == null) {
            return new Pair<>(null, "no method by name " + methodMetadata.getMethodName() + " found");
        }

        // constructorToUse = clazz.getDeclaredConstructor();

        final Method methodToUse = annotatedMethod;
        final Object targetObjectToUse = targetObject;
        Supplier<Pair<String,String>> toRun = () -> {
            String happyResult = null;
            try {
                happyResult = (String)methodToUse.invoke(targetObjectToUse);
            } catch (Throwable ex) {
                return Pair.with(null, "Exception when running Tacky method: " + ex.getMessage());
            }
            return Pair.with(happyResult, null);
        };
        return Pair.with(toRun, null);
    }
}
