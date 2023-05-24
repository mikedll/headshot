
package com.mikedll.headshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PropsController {

    private final Oauth2ConfigProperties oauth2Config;

    public PropsController(Oauth2ConfigProperties config) {
        this.oauth2Config = config;
    }
    
    @GetMapping("/props")
    public Map<String, String> printAllProps() {
        return Map.of("clientId", oauth2Config.clientId(),
                      "clientSecret", oauth2Config.clientSecret());
    }
}
