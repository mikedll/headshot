package com.mikedll.headshot;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class MyExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        System.out.println("I'm running the extension: " + testInstance);
    }
}
