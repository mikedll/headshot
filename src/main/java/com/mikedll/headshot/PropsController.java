
package com.mikedll.headshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PropsController {

    @GetMapping("/props")
    public Map<String, String> printAllProps() {
        return null;
    }
}
