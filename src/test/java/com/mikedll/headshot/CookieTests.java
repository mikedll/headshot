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
        SecretKey k = Cookie.genKey();
        Assertions.assertNotNull(k);
    }

    @Test
    public void testGenIv() throws NoSuchAlgorithmException, NoSuchPaddingException {
        IvParameterSpec iv = Cookie.genIv();
        Assertions.assertNotNull(iv);
    }
    
    @Test
    public void basic() {
        IvParameterSpec ivSpec = Cookie.genIv();
        Cookie c = new Cookie("fdfdsfsdf", ivSpec);
        String cipherText = c.encrypt("mike");
        Assertions.assertEquals("mike", c.decrypt(cipherText));
    }

}
