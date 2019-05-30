package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final LogoutController logoutController;

    public SecurityConfig(LogoutController logoutController) {
        this.logoutController = logoutController;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Need to configure to handle proper logout from Auth0
        http.authorizeRequests().anyRequest().authenticated()
                .and().oauth2Login()
                .and().logout()
                .logoutSuccessUrl("/")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler(logoutController);
    }
}