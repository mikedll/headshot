
package com.mikedll.headshot.experiment;

public class RequestHandler {

    public String path;

    public String method;

    public AnimalHandler handler;
    
    public RequestHandler(String path, String method, AnimalHandler handler) {
        this.path = path;
        this.method = method;
        this.handler = handler;
    }

    public String toString() {
        return method + " " + path;
    }
}
