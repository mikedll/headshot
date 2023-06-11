
package com.mikedll.headshot.experiment;

public class RequestSpec {

    public String path;

    public String method;

    public AnimalHandler handler;
    
    public RequestSpec(String path, String method, AnimalHandler handler) {
        this.path = path;
        this.method = method;
        this.handler = handler;
    }
}
