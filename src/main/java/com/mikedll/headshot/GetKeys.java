package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;

public class GetKeys {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println("Key: " + Cookie.genKey());
    }
}
