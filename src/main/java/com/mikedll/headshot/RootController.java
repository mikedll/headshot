package com.mikedll.headshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RootController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
