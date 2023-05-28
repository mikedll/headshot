package com.mikedll.headshot;

import java.util.Base64;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

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

public class CookieManager {

    public record VerifyResult(Map<String,Object> deserialized, boolean ok) {}
    
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

    public String cookieString(Map<String,Object> input)
        throws UnsupportedEncodingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(input);
        String signature = sign(serialized);
        return base64Encode(serialized.getBytes("UTF-8")) + "." + signature;
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
    public VerifyResult verify(String cookieString) throws UnsupportedEncodingException, JsonProcessingException {
        String[] split = cookieString.split("\\.");
        if(split.length != 2) {
            return new VerifyResult(null, false);
        }

        String encoded = split[0];
        String originalStr = base64DecodeStr(encoded);
        if(!sign(originalStr).equals(split[1])) {
            return new VerifyResult(null, false);
        }

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<LinkedHashMap<String,Object>> typeRef = new TypeReference<LinkedHashMap<String,Object>>() {};
        Map<String,Object> map = mapper.readValue(originalStr, typeRef);

        return new VerifyResult(map, true);
    }
}
