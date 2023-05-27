
package com.mikedll.headshot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private static final String authorizationRequestBaseUri = "oauth2/authorization";
    
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;


    @GetMapping("/oauth2_login")
    public String getLoginPage(Model model) {
        return "oauth2_login";
    }

}
