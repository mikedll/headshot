
package com.mikedll.headshot.controller;

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
}
