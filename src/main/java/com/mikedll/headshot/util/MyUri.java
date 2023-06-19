package com.mikedll.headshot.util;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

public class MyUri {
    public static URI from(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch(URISyntaxException ex) {
            throw new RuntimeException("Error in URI construction", ex);
        }
        return uri;
    }

    public static URI from(String url, List<NameValuePair> params) {
        URI uri = null;
        try {
            uri = new URIBuilder(from(url)).addParameters(params).build();
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Error in URI construction", ex);
        }
        return uri;
    }
}    
