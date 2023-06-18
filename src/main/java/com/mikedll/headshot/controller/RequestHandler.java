
package com.mikedll.headshot.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import java.util.Optional;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.javatuples.Pair;

import com.mikedll.headshot.Application;

public class RequestHandler {

    public PathMatchFunc tryMatch;

    public HttpMethod method;

    public RequestHandlerFunc handlerFunc;

    public RequestHandler(PathMatchFunc tryMatch, RequestHandlerFunc handlerFunc) {
        this.tryMatch = tryMatch;
        this.handlerFunc = handlerFunc;
    }

    public static Pair<RequestHandler, String> build(Class<?> clazz, MethodMetadata methodMetadata) {
        if(!Controller.class.isAssignableFrom(clazz)) {
            return Pair.with(null, String.format("Class %s of method '%s' is not a subclass of Controller",
                                                 clazz.getName(), methodMetadata.getMethodName()));
        }

        // Find class constructor
        Constructor<?>[] candidates = clazz.getDeclaredConstructors();
        Constructor<?> qualifyingCtor = null;
        for(Constructor<?> candidate : candidates) {
            Class[] parameterTypes = candidate.getParameterTypes();
            if(parameterTypes.length == 0) {
                qualifyingCtor = candidate;
                break;
            }
        }
        if(qualifyingCtor == null) {
            return new Pair<>(null, "found no suitable constructor");
        }

        // Find reflection version of given method
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
        
        RequestHandlerFunc requestHandlerFunc = buildHandlerFunc(clazz, annotatedMethod, qualifyingCtor);
        PathMatchFunc tryMatchFunc = buildMatchFunc(methodMetadata);
        RequestHandler created = new RequestHandler(tryMatchFunc, requestHandlerFunc);

        // Done!
        return Pair.with(created, null);        
    }

    /*
     * Returns func for servlet to call to respond to request.
     */ 
    public static RequestHandlerFunc buildHandlerFunc(Class<?> clazz, Method method, Constructor<?> constructor) {
        return (triplet) -> {
            Object targetObject = null;
            try {
                targetObject = constructor.newInstance();
            } catch (Throwable ex) {
                if(ex.getCause() != null) {
                    System.out.println(ex.getCause().getMessage());
                    ex.getCause().printStackTrace(System.out);
                    return ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage();
                } else {
                    return "Exception when instantiating " + clazz + ": " + ex.getMessage();
                }
            }

            Controller controller = (Controller)targetObject;
            controller.setRequest(triplet.getValue0());
            controller.setResponse(triplet.getValue1());
            controller.setExtractedParams(triplet.getValue2());
            controller.setDbConf(Application.dbConf);
            controller.setAssetFingerprinter(Application.assetFingerprinter);
            controller.setTemplateEngine(Application.templateEngine);
            if(!controller.prepare()) {
                // this is normal execution, not an error.
                return null;
            }

            try {
                method.invoke(controller);
            } catch (IllegalAccessException ex) {
                return "IllegalAccessException when running controller action: " + ex.getMessage();
            } catch (InvocationTargetException ex) {
                if(ex.getCause() != null) {
                    System.out.println(ex.getCause().getMessage());
                    ex.getCause().printStackTrace(System.out);
                    return ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage();
                } else {
                    // have never tested this path.
                    return "InvocationTargetException when running controller action: " + ex.getMessage();
                }
            }

            return null;
        };        
    }

    /*
     * Build path match func. Returns null on no match. Returns PathMatch on success.
     *
     * Func takes a (String path, HttpMethod method) pair as input.
     */
    public static PathMatchFunc buildMatchFunc(MethodMetadata methodMetadata) {
        Map<String, Object> attrs = methodMetadata.getAnnotationAttributes(Scanner.ANNOTATION);
        HttpMethod httpMethod = (HttpMethod)attrs.get("method");
        String annotationPath = (String)attrs.get("path");
        PathParamMatcher withParams = PathParamMatcher.build(annotationPath).orElse(null);
        PathMatchFunc matchFunc = null;
        if(withParams == null) {
            matchFunc = (incomingRequest) -> {
                if(annotationPath.equals(incomingRequest.getValue0()) && httpMethod.equals(incomingRequest.getValue1())) {
                    return Optional.ofNullable(new PathMatch(incomingRequest.getValue0(), new HashMap<>()));
                } else {
                    return Optional.empty();
                }
            };
        } else {
            matchFunc = (incomingRequest) -> {
                if(!httpMethod.equals(incomingRequest.getValue1())) {
                    return Optional.empty();
                }

                return withParams.match(incomingRequest.getValue0());
            };
        }

        return matchFunc;            
    }
}
