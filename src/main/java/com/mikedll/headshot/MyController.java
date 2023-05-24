package com.mikedll.headshot;

import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/")
    public String index() {
        return "Hello this is the web action running!";
    }
}
