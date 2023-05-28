package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class CookieTests {

    private final String key = "eVKgwkis9APaD2o2/suPAv9sgs156+fMTBDDbM1vgwU=";
    
    @Test
    public void testGenKey() throws NoSuchAlgorithmException {
        String k = Cookie.genKey();
        Assertions.assertNotEquals("", k);
    }

    @Test
    void testDecode() {
        Cookie c = new Cookie(key);
        Assertions.assertEquals("mike goes to the store", c.base64DecodeStr("bWlrZSBnb2VzIHRvIHRoZSBzdG9yZQ=="));
    }

    public Map<String,Object> toSign() {
        Map<String, Object> toSign = new LinkedHashMap<String, Object>();
        Map<String,Object> p1 = new LinkedHashMap<String,Object>();
        p1.put("name", "Mike");
        p1.put("age", 30);
        toSign.put("mike", p1);

        Map<String,Object> p2 = new LinkedHashMap<String,Object>();
        p2.put("name", "Harry");
        p2.put("age", 40);
        toSign.put("harry", p2);
        
        toSign.put("error", "You didn't fill out the form");
        toSign.put("oauth2code", "fdsfdsfdsfsf");

        List<Map<String,Object>> people = new ArrayList<Map<String,Object>>();
        Map<String,Object> p3 = new LinkedHashMap<String,Object>();
        p3.put("name", "Herman");
        p3.put("age", 25);
        people.add(p3);
        
        Map<String,Object> p4 = new LinkedHashMap<String,Object>();
        p4.put("name", "Sally");
        p4.put("age", 21);
        people.add(p3);

        toSign.put("people", people);
        
        return toSign;
    }
    
    @Test
    @SuppressWarnings("unchecked")    
    public void testVerifySucceeds() throws UnsupportedEncodingException, JsonProcessingException {
        Cookie c = new Cookie(key);

        Map<String, Object> toSign = toSign();
        String cookieString = c.cookieString(toSign);
        Cookie.VerifyResult result = c.verify(cookieString);
        
        Assertions.assertTrue(result.ok());
        Assertions.assertEquals(toSign, result.deserialized());

        Map<String,Object> deserialized = (Map<String,Object>)result.deserialized();
        Map<String,Object> mike = (Map<String,Object>)deserialized.get("mike");
        
        Assertions.assertEquals(mike.get("age"), 30);
        Assertions.assertEquals(mike.get("name"), "Mike");

        List<Map<String,Object>> people = (List<Map<String,Object>>)result.deserialized().get("people");
        Assertions.assertEquals(2, people.size());
        Map<String,Object> herman = (Map<String,Object>)people.get(0);
        Assertions.assertEquals(herman.get("name"), "Herman");
        Assertions.assertEquals(herman.get("age"), 25);
    }

    @Test
    public void testVerifyFail() throws UnsupportedEncodingException, JsonProcessingException {
        Cookie c = new Cookie(key);
        Map<String, Object> toSign = toSign();
        String cookieString = c.cookieString(toSign);

        String[] split = cookieString.split("\\.");
        String badSig = Cookie.base64Encode("notValidSignature".getBytes("UTF-8"));
        String badCookieString = split[0] + "." + badSig;
        Assertions.assertFalse(c.verify(badCookieString).ok());
    }

    @Test
    public void testVerifyFailJunk() throws UnsupportedEncodingException, JsonProcessingException {
        Cookie c = new Cookie(key);
        Assertions.assertFalse(c.verify("bullcrap").ok());
    }
    
}
