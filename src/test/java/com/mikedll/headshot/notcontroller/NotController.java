package com.mikedll.headshot.notcontroller;

import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.controller.Request;

public class NotController {

    @Request(path="/nowhere")
    public void index() {
    }
}
