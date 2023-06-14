package com.mikedll.headshot;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.NoSuchMethodException;
import java.lang.InstantiationException;
    
public abstract class TestSuite {

    private static Map<Class<?>, Object> suiteInstances = new HashMap<>();
    
    private boolean setup;
    
    public static <T> T getSuite(Class<T> suiteClass) {
        if(suiteInstances.get(suiteClass) == null) {
            T newInstance = null;
            try {
                newInstance = suiteClass.getDeclaredConstructor().newInstance();
            } catch (Throwable ex) {
                throw new RuntimeException("Error when instantiating " + suiteClass, ex);
            }
            suiteInstances.put(suiteClass, newInstance);
        }

        return suiteClass.cast(suiteInstances.get(suiteClass));
    }

    /*
     * Returns true on success, false on failure.
     */
    protected abstract boolean doSetUp() throws IOException;

    protected abstract void doTearDown();

    protected abstract boolean doBeforeTest();
    
    public boolean setUp() throws IOException {
        teardownAllOtherSuites();

        if(setup) {
            return true;
        }

        this.setup = doSetUp();
        return this.setup;
    }

    public boolean beforeTest() {
        if(!setup) {
            return false;
        }

        return doBeforeTest();
    }

    /*
     * Return true on success, false on failure.
     */
    private void teardownAllOtherSuites() {
        this.suiteInstances.keySet().forEach(k -> {
                if(this.getClass().isAssignableFrom(k)) {
                    return;
                }

                ((TestSuite)this.suiteInstances.get(k)).doTearDown();
            });
    }

}
