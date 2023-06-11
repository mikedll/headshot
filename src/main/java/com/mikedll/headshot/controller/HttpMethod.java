
package com.mikedll.headshot.controller;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;

    public static HttpMethod fromServletReq(String method) {
        if(method.equals("GET")) {
            return GET;
        } else if(method.equals("POST")) {
            return POST;
        } else if(method.equals("PUT") || method.equals("UPDATE")) {
            return PUT;
        } else if(method.equals("DELETE")) {
            return DELETE;
        } else {
            throw new RuntimeException("unexpected method to fromServletReq: " + method);
        }
    }
}
