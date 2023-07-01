package com.mikedll.headshot.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.mikedll.headshot.Application;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.SimpleSuite;

public class CookieManagerTests {

    private Application app;
    
    @BeforeEach
    public void setup() {
        this.app = TestSuite.getSuite(SimpleSuite.class).app;
    }
    
    @Test
    public void testGenKey() throws NoSuchAlgorithmException {
        String k = CookieManager.genKey();
        Assertions.assertNotEquals("", k);
    }

    @Test
    void testDecode() {
        CookieManager c = new CookieManager(app.config.cookieSigningKey);
        Assertions.assertEquals("mike goes to the store", c.base64DecodeStr("bWlrZSBnb2VzIHRvIHRoZSBzdG9yZQ=="));
    }

    public Map<String,Object> toSign() {
        Map<String, Object> toSign = new HashMap<String, Object>();
        Map<String,Object> p1 = new HashMap<String,Object>();
        p1.put("name", "Mike");
        p1.put("age", 30);
        toSign.put("mike", p1);

        Map<String,Object> p2 = new HashMap<String,Object>();
        p2.put("name", "Harry");
        p2.put("age", 40);
        toSign.put("harry", p2);
        
        toSign.put("error", "You didn't fill out the form");
        toSign.put("oauth2code", "fdsfdsfdsfsf");

        List<Map<String,Object>> people = new ArrayList<Map<String,Object>>();
        Map<String,Object> p3 = new HashMap<String,Object>();
        p3.put("name", "Herman");
        p3.put("age", 25);
        people.add(p3);
        
        Map<String,Object> p4 = new HashMap<String,Object>();
        p4.put("name", "Sally");
        p4.put("age", 21);
        people.add(p3);

        toSign.put("people", people);
        
        return toSign;
    }
    
    @Test
    @SuppressWarnings("unchecked")    
    public void testVerifySucceeds() {
        CookieManager c = new CookieManager(app.config.cookieSigningKey);

        Map<String, Object> toSign = toSign();
        Pair<String,String> strResult = c.cookieString(app.jsonObjectMapper, toSign);
        Assertions.assertNull(strResult.getValue1());
        Pair<Map<String,Object>,String> verifyResult = c.verify(app.jsonObjectMapper, strResult.getValue0());
        
        Assertions.assertNull(verifyResult.getValue1(), "verify ok");
        Assertions.assertEquals(toSign, verifyResult.getValue0(), "correct map");
    }

    @Test
    public void testVerifyFail() throws UnsupportedEncodingException {
        CookieManager c = new CookieManager(app.config.cookieSigningKey);
        Map<String, Object> toSign = toSign();
        Pair<String,String> strResult = c.cookieString(app.jsonObjectMapper, toSign);
        Assertions.assertNull(strResult.getValue1());

        String[] split = strResult.getValue0().split("\\.");
        String badSig = CookieManager.base64Encode("notValidSignature".getBytes("UTF-8"));
        String badCookieString = split[0] + "." + badSig;
        Assertions.assertNotNull(c.verify(app.jsonObjectMapper, badCookieString).getValue1(), "find error");
    }

    @Test
    public void testVerifyFailJunk() {
        CookieManager c = new CookieManager(app.config.cookieSigningKey);
        Assertions.assertNotNull(c.verify(app.jsonObjectMapper, "bullcrap").getValue1(), "find error");
    }
    
}
