package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
    
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
    
    @Test
    public void testVerify() {
        Cookie c = new Cookie(key);
        String toSign = "mike goes to the store";
        String cookieString = c.cookieString(toSign);

        String[] split = cookieString.split("\\.");
        Assertions.assertEquals(2, split.length);
        String encoded = split[0];
        String sig = split[1];
        Assertions.assertTrue(c.verify(encoded, sig));
    }

    @Test
    public void testVerifyFail() throws UnsupportedEncodingException {
        Cookie c = new Cookie(key);
        String toSign = "mike goes to the store";
        String cookieString = c.cookieString(toSign);
        
        String[] split = cookieString.split("\\.");
        Assertions.assertEquals(2, split.length);
        String encoded = split[0];
        String sig = Cookie.base64Encode("notValidSignature".getBytes("UTF-8"));
        Assertions.assertFalse(c.verify(encoded, sig));
    }

}
