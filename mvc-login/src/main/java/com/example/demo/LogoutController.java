package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Needed to perform proper logout with Auth0
 */
@Controller
public class LogoutController extends SecurityContextLogoutHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
    private String domain;

    public LogoutController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    //TODO - verify this is the right extension point, just a best guess for now!!
    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       Authentication authentication) {


        super.logout(httpServletRequest, httpServletResponse, authentication);

        String logoutUrl = String.format(
                "%sv2/logout?client_id=%s&returnTo=%s",
                this.domain,
                getClientRegistration().getClientId(),
                "http://localhost:3000" // TODO don't hardcode!
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
