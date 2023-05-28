package com.mikedll.headshot;

import java.util.Base64;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class Cookie {

    public final SecretKey keySpec;

    public static final String algorithm = "HmacSHA256";
        
    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64Decode(String s) {
        return Base64.getDecoder().decode(s);
    }

    public static String base64DecodeStr(String input) {
        return new String(base64Decode(input), StandardCharsets.UTF_8);
    }
    
    public static String genKey() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
 
        // 256 bits.
        // https://crypto.stackexchange.com/questions/31473/what-size-should-the-hmac-key-be-with-sha-256
        keyGenerator.init(256, secureRandom);
        
        return base64Encode(keyGenerator.generateKey().getEncoded());
    }

    public Cookie(String key) {
        byte[] decodedKey = base64Decode(key);
        this.keySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }

    public String cookieString(String input) {
        String signature = sign(input);
        return base64Encode(input.getBytes()) + "." + signature;
    }

    /*
     * Sign a string.
     */
    public String sign(String input) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
                
            byte[] signature = mac.doFinal(input.getBytes());
            return base64Encode(signature);
        } catch (Exception ex) {
            System.out.println("Failed to sign string: " + ex.getMessage());
        }

        return null;
    }

    /*
     * Verify a signature.
     */
    public boolean verify(String encoded, String signature) {
        String originalStr = Cookie.base64DecodeStr(encoded);
        return signature.equals(sign(originalStr));
    }
}
