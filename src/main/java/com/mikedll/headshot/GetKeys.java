package com.mikedll.headshot;

import java.security.NoSuchAlgorithmException;

import com.mikedll.headshot.controller.CookieManager;

public class GetKeys {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println("Key: " + CookieManager.genKey());
    }
}
