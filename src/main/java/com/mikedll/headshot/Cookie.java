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

    public final String key;

    public final String iv;

    public static final String algorithm = "AES";
    
    public static final String scheme = algorithm + "/CBC/PKCS5PADDING";
    
    public Cookie(String key, String iv) {
        this.key = key;
        this.iv = iv;
    }
    
    public static SecretKey genKey() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
 
        // 256 bits.
        keyGenerator.init(256, secureRandom);
        
        return keyGenerator.generateKey();
    }

    public static IvParameterSpec genIv() throws NoSuchAlgorithmException, NoSuchPaddingException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[Cipher.getInstance(scheme).getBlockSize()];
        secureRandom.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /*
    /*
     * Plaintext string.
     */
    public String encrypt(String plainText) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(scheme);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
                
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
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
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(scheme);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(original);
        } catch (Exception ex) {
            System.out.println("Failed to decrypt string: " + ex.getMessage());
        }

        return null;
    }
}
