package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;

public class CookieTests {

    @Test
    public void testGenKey() throws NoSuchAlgorithmException {
        String k = Cookie.genKey();
        Assertions.assertNotEquals("", k);
    }

    @Test
    public void testGenIv() throws NoSuchAlgorithmException, NoSuchPaddingException {
        String iv = Cookie.genIv();
        Assertions.assertNotEquals("", iv);
    }
    
    @Test
    public void basic() throws NoSuchAlgorithmException, NoSuchPaddingException {
        String keyStr = Cookie.genKey();
        String ivStr = Cookie.genIv();
        
        Cookie c = new Cookie(keyStr, ivStr);
        String plain = "mike went to the store";
        String cipherText = c.encrypt(plain);
        Assertions.assertEquals(plain, c.decrypt(cipherText));
    }

}
