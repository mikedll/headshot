package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class CookieManagerTests {

    private final String key = "eVKgwkis9APaD2o2/suPAv9sgs156+fMTBDDbM1vgwU=";
    
    @Test
    public void testGenKey() throws NoSuchAlgorithmException {
        String k = CookieManager.genKey();
        Assertions.assertNotEquals("", k);
    }

    @Test
    void testDecode() {
        CookieManager c = new CookieManager(key);
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
    public void testVerifySucceeds() throws UnsupportedEncodingException, JsonProcessingException {
        CookieManager c = new CookieManager(key);

        Map<String, Object> toSign = toSign();
        String cookieString = c.cookieString(toSign);
        CookieManager.VerifyResult result = c.verify(cookieString);
        
        Assertions.assertTrue(result.ok());
        Assertions.assertEquals(toSign, result.deserialized());
    }

    @Test
    public void testVerifyFail() throws UnsupportedEncodingException, JsonProcessingException {
        CookieManager c = new CookieManager(key);
        Map<String, Object> toSign = toSign();
        String cookieString = c.cookieString(toSign);

        String[] split = cookieString.split("\\.");
        String badSig = CookieManager.base64Encode("notValidSignature".getBytes("UTF-8"));
        String badCookieString = split[0] + "." + badSig;
        Assertions.assertFalse(c.verify(badCookieString).ok());
    }

    @Test
    public void testVerifyFailJunk() throws UnsupportedEncodingException, JsonProcessingException {
        CookieManager c = new CookieManager(key);
        Assertions.assertFalse(c.verify("bullcrap").ok());
    }
    
}
