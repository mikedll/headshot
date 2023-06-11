package com.mikedll.headshot.controller;

import java.lang.ClassNotFoundException;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;

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

    private final String annotation = "com.mikedll.headshot.controller.Request";

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

            Set<MethodMetadata> methods = classMetadata.getAnnotatedMethods(annotation);
            Class clazz = null;
            for(MethodMetadata method : methods) {
                if(clazz == null) {
                    try {
                        clazz = Class.forName(classMetadata.getClassName());
                    } catch (ClassNotFoundException ex) {
                        return Pair.with(null, "ClassNotFoundException for " + classMetadata.getClassName() + ": " + ex.getMessage());
                    }
                }
                Pair<RequestHandlerFunc, String> toRun = buildHandler(clazz, method);
                if(toRun.getValue1() != null) {
                    return Pair.with(null, "Error when building handler for method " + method + ": " + toRun.getValue1());
                }
                Map<String, Object> attrs = method.getAnnotationAttributes(annotation);
                HttpMethod httpMethod = (HttpMethod)attrs.get("method");
                requestHandlers.add(new RequestHandler((String)attrs.get("path"), httpMethod, toRun.getValue0()));
            }
        }

        return Pair.with(requestHandlers, null);
    }

    public Pair<RequestHandlerFunc, String> buildHandler(Class clazz, MethodMetadata methodMetadata) {
        Constructor[] candidates = clazz.getDeclaredConstructors();
        Constructor qualifyingCtor = null;
        for(Constructor candidate : candidates) {
            Class[] parameterTypes = candidate.getParameterTypes();
            if(parameterTypes.length == 0) {
                qualifyingCtor = candidate;
                break;
            }
        }

        if(qualifyingCtor == null) {
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

        final Method methodToUse = annotatedMethod;
        final Constructor ctorToUse = qualifyingCtor;
        RequestHandlerFunc toRun = (pair) -> {
            Object targetObject = null;
            try {
                targetObject = ctorToUse.newInstance();
            } catch (Throwable ex) {
                return "Exception when instantiating " + clazz + ": " + ex.getMessage();
            }

            Controller controller = (Controller)targetObject;
            controller.setRequest(pair.getValue0());
            controller.setResponse(pair.getValue1());
            if(!controller.prepare()) {
                // this is normal execution, not an error.
                return null;
            }

            try {
                methodToUse.invoke(controller);
            } catch (IllegalAccessException ex) {
                return "IllegalAccessException when running controller action: " + ex.getMessage();
            } catch (InvocationTargetException ex) {
                if(ex.getCause() != null) {
                    return ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage();
                } else {
                    // have never tested this path.
                    return "InvocationTargetException when running controller action: " + ex.getMessage();
                }
            }

            // Arrays.asList(Thread.currentThread().getStackTrace()).forEach(t -> System.out.println(t));

            return null;
        };
        return Pair.with(toRun, null);
    }
}
