package com.mikedll.headshot;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.NoSuchMethodException;
import java.lang.InstantiationException;
    
public abstract class TestSuite {

    private static Map<Class<?>, Object> suiteInstances = new HashMap<>();

    private static boolean allSuitesSetup;
    
    private boolean setup;
    
    public static <T extends TestSuite> T getSuite(Class<T> suiteClass) {
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

    protected abstract boolean doBeforeEach();
    
    public boolean setUp() throws IOException {
        if(setup) {
            return true;
        }

        setUpAllSuites();
        teardownAllOtherSuites();
        this.setup = doSetUp();
        return this.setup;
    }

    public boolean beforeEach() {
        if(!setup) {
            return false;
        }

        return doBeforeEach();
    }

    private void teardownAllOtherSuites() {
        this.suiteInstances.keySet().forEach(k -> {
                if(this.getClass().isAssignableFrom(k)) {
                    return;
                }

                ((TestSuite)this.suiteInstances.get(k)).doTearDown();
            });
    }

    private static void setUpAllSuites() {
        if(allSuitesSetup == false) {
            
            Env.cookieSigningKey = "eVKgwkis9APaD2o2/suPAv9sgs156+fMTBDDbM1vgwU=";
            Env.env = "test";
            
            allSuitesSetup = true;
        }
    }

}
