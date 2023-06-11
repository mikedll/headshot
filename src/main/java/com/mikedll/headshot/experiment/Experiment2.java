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

public class Experiment2 {

    private String[] names = new String[] { "Minny", "Mickey", "Tom" };
    private Integer[] ages = new Integer[] { 21, 25, 39 };

    public void run() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String path = "classpath*:com/mikedll/headshot/experiment/**/*.class";

        final String tackyAnnotation = "com.mikedll.headshot.experiment.Tacky";
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        Resource[] resources = null;
        try {
            resources = resolver.getResources(path);
        } catch(IOException ex) {
            System.out.println("IOException when scanning for controllers: " + ex.getMessage());
            return;
        }

        List<RequestHandler> requestHandlers = new ArrayList<>();
        for(Resource resource : resources) {
            MetadataReader metadataReader = null;
            try {
                metadataReader = metadataReaderFactory.getMetadataReader(resource);
            } catch(IOException ex) {
                System.out.println("IOException when retrieving metadata reader: " + ex.getMessage());
                return;
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

        runTests(requestHandlers);
    }

    public void runTests(List<RequestHandler> requestHandlers) {
        if(requestHandlers.size() == 0) {
            System.out.println("Request specs size was 0, returning early");
            return;
        }

        System.out.println("Request handlers:");
        requestHandlers.forEach(rh -> System.out.println(rh));

        int count = 500000;
        List<Request> requests = new ArrayList<>(count);
        
        for(int i = 0; i < count; i++) {
            String name = names[(int)(Math.random() * 3.0)];
            Integer age = ages[(int)(Math.random() * 3.0)];
            RequestHandler requestHandler = requestHandlers.get((int)(Math.random() * requestHandlers.size()));

            requests.add(new Request(requestHandler.path, requestHandler.method, name, age));
        }

        requests.forEach(r -> dispatch(requestHandlers, r));
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
