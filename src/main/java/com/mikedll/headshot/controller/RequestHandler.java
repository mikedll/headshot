
package com.mikedll.headshot.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.javatuples.Pair;

import com.mikedll.headshot.Application;

public class RequestHandler {

    public String path;

    public HttpMethod method;

    public RequestHandlerFunc func;
    
    public RequestHandler(String path, HttpMethod method, RequestHandlerFunc func) {
        this.path = path;
        this.method = method;
        this.func = func;
    }

    public String toString() {
        return method + " " + path;
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

        // Build lambda Function that Servlet will call
        final Method methodToUse = annotatedMethod;
        final Constructor ctorToUse = qualifyingCtor;
        RequestHandlerFunc toRun = (pair) -> {
            Object targetObject = null;
            try {
                targetObject = ctorToUse.newInstance();
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
            controller.setRequest(pair.getValue0());
            controller.setResponse(pair.getValue1());
            controller.setDbConf(Application.dbConf);
            controller.setAssetFingerprinter(Application.assetFingerprinter);
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

        // Set path/Http method of this handler, e.g. "/profile", GET
        Map<String, Object> attrs = methodMetadata.getAnnotationAttributes(Scanner.ANNOTATION);
        HttpMethod httpMethod = (HttpMethod)attrs.get("method");
        RequestHandler created = new RequestHandler((String)attrs.get("path"), httpMethod, toRun);

        // Done!
        return Pair.with(created, null);        
    }
}
