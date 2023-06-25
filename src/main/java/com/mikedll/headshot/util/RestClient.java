package com.mikedll.headshot.util;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.javatuples.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.NameValuePair;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.WWWFormCodec;

import com.mikedll.headshot.JsonMarshal;

public class RestClient {

    public Pair<String,String> get(URI uri) {
        Map<String,String> headers = new HashMap<>();
        return get(uri, headers);
    }

    public Pair<String,String> get(URI uri, Map<String,String> headers) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            headers.keySet().forEach(k -> {
                    httpGet.addHeader(k, headers.get(k));
                });

            String result = httpclient.execute(httpGet, response -> {
                    return EntityUtils.toString(response.getEntity());
                });
            System.out.println(result);
            return Pair.with(result, null);
        } catch (IOException ex) {
            return Pair.with(null, "Error in REST call: " + ex.getMessage());
        }
    }

    public <T> Pair<T,String> get(URI uri, Map<String,String> headers, TypeReference<T> typeRef) {
        Pair<String,String> rawResult = get(uri, headers);
        if(rawResult.getValue1() != null) {
            return Pair.with(null, rawResult.getValue1());
        }

        Pair<T,String> unmarshalResult = JsonMarshal.unmarshal(rawResult.getValue0(), typeRef);
        if(unmarshalResult.getValue1() != null) {
            return Pair.with(null, unmarshalResult.getValue1());
        }

        return Pair.with(unmarshalResult.getValue0(), null);
    }

    public Pair<String,String> post(URI uri, Map<String,String> headers, List<NameValuePair> params) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(uri);

            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            String result = httpclient.execute(httpPost, response -> {
                    return EntityUtils.toString(response.getEntity());
                });
            return Pair.with(result, null);
        } catch (IOException ex) {
            return Pair.with(null, "Error in REST call: " + ex.getMessage());
        }
    }

    public Pair<List<NameValuePair>,String> nvParamsPost(URI uri, Map<String,String> headers, List<NameValuePair> params) {
        Pair<String,String> bodyResult = post(uri, headers, params);
        if(bodyResult.getValue1() != null) {
            return Pair.with(null, bodyResult.getValue1());
        }
        
        List<NameValuePair> result = WWWFormCodec.parse(bodyResult.getValue0(), StandardCharsets.UTF_8);
        return Pair.with(result, null);
    }
    
    public <T> Pair<T,String> post(URI uri, Map<String,String> headers, List<NameValuePair> params, TypeReference<T> typeRef) {
        Pair<String,String> rawResult = post(uri, headers, params);
        if(rawResult.getValue1() != null) {
            return Pair.with(null, rawResult.getValue1());
        }

        Pair<T,String> unmarshalResult = JsonMarshal.unmarshal(rawResult.getValue0(), typeRef);
        if(unmarshalResult.getValue1() != null) {
            return Pair.with(null, unmarshalResult.getValue1());
        }

        return Pair.with(unmarshalResult.getValue0(), null);
    }
    
}
