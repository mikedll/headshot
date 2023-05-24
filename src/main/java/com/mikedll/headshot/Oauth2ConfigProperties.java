
package com.mikedll.headshot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2")
public record Oauth2ConfigProperties(String clientId, String clientSecret) {
}
 
