package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value( "${auth0.audience}" )
    private String audience;

    private ClientRegistrationRepository clientRegistrationRepository;

    private final LogoutController logoutController;

    @Autowired
    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository, LogoutController logoutController) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.logoutController = logoutController;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and().oauth2Login()
                .authorizationEndpoint()
                .authorizationRequestResolver(new CustomAuthorizationRequestResolver(
                        this.clientRegistrationRepository, this.audience))
                .and().and().logout()
                .logoutSuccessUrl("/")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler(logoutController);
    }
}
