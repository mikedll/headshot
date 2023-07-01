package com.mikedll.headshot.controller;

import java.util.Base64;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.javatuples.Pair;

public class CookieManager {
    
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

    public CookieManager(String key) {
        byte[] decodedKey = base64Decode(key);
        this.keySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }

    /*
     * @todo encode and retrieve user_id as a Long
     */
    public Pair<String,String> cookieString(ObjectMapper mapper, Map<String,Object> input) {
        String marshalled = null;
        String signature = null;
        String ret = null;
        try {
            marshalled = mapper.writeValueAsString(input);
            signature = sign(marshalled);
            ret = base64Encode(marshalled.getBytes("UTF-8")) + "." + signature;
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "JsonProcessingException: " + ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            return Pair.with(null, "UnsupportedEncodingException: " + ex.getMessage());
        }
        return Pair.with(ret, null);
    }

    /*
     * Sign a string.
     */
    public String sign(String input) throws UnsupportedEncodingException {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
                
            byte[] signature = mac.doFinal(input.getBytes("UTF-8"));
            return base64Encode(signature);
        } catch (Exception ex) {
            System.out.println("Failed to sign string: " + ex.getMessage());
        }

        return null;
    }

    /*
     * Verify a signature.
     */
    public Pair<Map<String,Object>,String> verify(ObjectMapper mapper, String cookieString) {
        String[] split = cookieString.split("\\.");
        if(split.length != 2) {
            return Pair.with(null, "invalid string format");
        }

        String encoded = split[0];
        String originalStr = base64DecodeStr(encoded);
        String signed = null;
        try {
            signed = sign(originalStr);
        } catch (UnsupportedEncodingException ex) {
            return Pair.with(null, "UnsupportedEncodingException: " + ex.getMessage());
        }
        if(!signed.equals(split[1])) {
            return Pair.with(null, "invalid signature");
        }

        try {
            return Pair.with(mapper.readValue(originalStr, new TypeReference<LinkedHashMap<String,Object>>() {}), null);
        } catch(JsonProcessingException ex) {
            return Pair.with(null, "JsonProcessingException: " + ex.getMessage());
        }
    }
}
