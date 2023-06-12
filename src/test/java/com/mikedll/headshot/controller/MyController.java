package com.mikedll.headshot.controller;

public class MyController extends Controller {
        
    @Request(path="/junit/sample")
    public void sample() {
        render("hello", defaultCtx());
    }
}
