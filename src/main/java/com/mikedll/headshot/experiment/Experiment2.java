package com.mikedll.headshot.experiment;

import java.lang.ClassNotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.Math;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.annotation.MergedAnnotations;

import org.javatuples.Pair;

public class Experiment2 {

    public List<RequestHandler> findHandlers() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String path = "classpath*:com/mikedll/headshot/experiment/**/*.class";

        final String tackyAnnotation = "com.mikedll.headshot.experiment.Tacky";
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        Resource[] resources = null;
        try {
            resources = resolver.getResources(path);
        } catch(IOException ex) {
            System.out.println("IOException when scanning for controllers: " + ex.getMessage());
            return null;
        }

        List<RequestHandler> requestHandlers = new ArrayList<>();
        for(Resource resource : resources) {
            MetadataReader metadataReader = null;
            try {
                metadataReader = metadataReaderFactory.getMetadataReader(resource);
            } catch(IOException ex) {
                System.out.println("IOException when retrieving metadata reader: " + ex.getMessage());
                return null;
            }
            AnnotationMetadata classMetadata = metadataReader.getAnnotationMetadata();

            Set<MethodMetadata> methods = classMetadata.getAnnotatedMethods(tackyAnnotation);
            Class clazz = null;
            for(MethodMetadata method : methods) {
                if(clazz == null) {
                    try {
                        clazz = Class.forName(classMetadata.getClassName());
                    } catch (ClassNotFoundException ex) {
                        System.out.println("ClassNotFoundException for " + classMetadata.getClassName() + ": " + ex.getMessage());
                        break;
                    }
                }
                Pair<AnimalHandler, String> toRun = buildHandler(clazz, method);
                if(toRun.getValue1() != null) {
                    System.out.println("Error when building handler for method " + method + ": " + toRun.getValue1());
                    continue;
                }
                Map<String, Object> attrs = method.getAnnotationAttributes(tackyAnnotation);
                requestHandlers.add(new RequestHandler((String)attrs.get("path"), (String)attrs.get("method"), toRun.getValue0()));
            }
        }

        return requestHandlers;
    }

    public void dispatch(List<RequestHandler> requestHandlers, Request request) {
        RequestHandler matchinHandler = requestHandlers
            .stream()
            .filter(rh -> rh.path.equals(request.path()) && rh.method.equals(request.method()))
            .findAny()
            .orElse(null);

        if(matchinHandler == null) {
            throw new RuntimeException("failed to find matching handler");
        }
        
        Pair<String,String> result = matchinHandler.handler.apply(Pair.with(request.name(), request.age()));
        if(result.getValue1() != null) {
            throw new RuntimeException("Error when running function: " + result.getValue1());
        }
    }

    public void dispatchBasic(Request request) {
        if(request.path().equals("/bark") && request.method().equals("GET")) {
            Dog animal = new Dog(request.name(), request.age());
            animal.bark();
        } else if(request.path().equals("/rollover") && request.method().equals("POST")) {
            Dog animal = new Dog(request.name(), request.age());
            animal.rollover();            
        } else if(request.path().equals("/purr") && request.method().equals("GET")) {
            Cat animal = new Cat(request.name(), request.age());
            animal.purr();            
        } else {
            throw new RuntimeException("unrecognized path/method: " + request.path() + "/" + request.method());
        }
    }

    public Pair<AnimalHandler, String> buildHandler(Class clazz, MethodMetadata methodMetadata) {
        Constructor[] candidates = clazz.getDeclaredConstructors();
        Constructor qualifyingCtor = null;
        for(Constructor candidate : candidates) {
            Class[] parameterTypes = candidate.getParameterTypes();
            if(parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == Integer.class) {
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
        AnimalHandler toRun = (pair) -> {
            String happyResult = null;

            Object targetObject = null;
            try {
                targetObject = ctorToUse.newInstance(new Object[] { pair.getValue0(), pair.getValue1() });
            } catch (Throwable ex) {
                return new Pair<>(null, "Exception when instantiating " + clazz + ": " + ex.getMessage());
            }
            
            try {
                happyResult = (String)methodToUse.invoke(targetObject);
            } catch (Throwable ex) {
                return Pair.with(null, "Exception when running Tacky method: " + ex.getMessage());
            }
            return Pair.with(happyResult, null);
        };
        return Pair.with(toRun, null);
    }
}
