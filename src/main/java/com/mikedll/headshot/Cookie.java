package com.mikedll.headshot;

import java.util.Base64;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class Cookie {

    public final SecretKey keySpec;

    public final IvParameterSpec ivSpec;

    public static final String algorithm = "AES";
    
    public static final String scheme = algorithm + "/CBC/PKCS5PADDING";
    
    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64Decode(String s) {
        return Base64.getDecoder().decode(s);
    }
    
    public static String genKey() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
 
        // 256 bits.
        keyGenerator.init(256, secureRandom);
        
        return base64Encode(keyGenerator.generateKey().getEncoded());
    }

    public static String genIv() throws NoSuchAlgorithmException, NoSuchPaddingException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[Cipher.getInstance(scheme).getBlockSize()];
        secureRandom.nextBytes(iv);
        return base64Encode(iv);
    }

    public Cookie(String key, String iv) {
        byte[] decodedKey = base64Decode(key);
        this.keySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
        this.ivSpec = new IvParameterSpec(base64Decode(iv));
    }    

    /*
    /*
     * Plaintext string.
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(scheme);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return base64Encode(encrypted);
        } catch (Exception ex) {
            System.out.println("Failed to encrypt string: " + ex.getMessage());
        }

        return null;
    }

    /*
     * Enrypted string.
     */
    public String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(scheme);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] original = cipher.doFinal(base64Decode(cipherText));
            return new String(original);
        } catch (Exception ex) {
            System.out.println("Failed to decrypt string: " + ex.getMessage());
        }

        return null;
    }
}
