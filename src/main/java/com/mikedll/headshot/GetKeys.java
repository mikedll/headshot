package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

public class GetKeys {

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException {
        System.out.println("IV: " + Cookie.genIv());
        System.out.println("Key: " + Cookie.genKey());
    }
}
