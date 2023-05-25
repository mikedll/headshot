package com.mikedll.headshot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

@SpringBootApplication
public class Application {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                                   .requestMatchers("/oauth2_login", "/error")
                                   .permitAll()
                                   .anyRequest().authenticated())
            .oauth2Login()
            .loginPage("/oauth2_login");
        return http.build();
    }

    // @Bean
    public CommandLineRunner printClientRegistrations(ApplicationContext ctx) {
        return args -> {
            Iterable<ClientRegistration> clientRegistrations = null;
            ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository)
                .as(Iterable.class);

            if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
                clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
            }

            clientRegistrations.forEach(registration -> System.out.println("OAuth2 Client: " + registration.getClientName()));
        };
    }
    
}
