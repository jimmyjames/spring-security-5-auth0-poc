package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Needed to perform SSO logout with Auth0
 */
@Controller
public class LogoutHandler extends SecurityContextLogoutHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String domain;

    public LogoutHandler(ClientRegistrationRepository clientRegistrationRepository,
                         @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}") String domain) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.domain = domain;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       Authentication authentication) {

        super.logout(httpServletRequest, httpServletResponse, authentication);

        String returnTo = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString();
        String logoutUrl = String.format(
                "%sv2/logout?client_id=%s&returnTo=%s",
                this.domain,
                getClientRegistration().getClientId(),
                returnTo
        );

        try {
            httpServletResponse.sendRedirect(logoutUrl);
        } catch (IOException ioe) {
            log.error("Error redirecting to logout URL", ioe);
        }

    }

    private ClientRegistration getClientRegistration() {
        return this.clientRegistrationRepository.findByRegistrationId("auth0");
    }
}
