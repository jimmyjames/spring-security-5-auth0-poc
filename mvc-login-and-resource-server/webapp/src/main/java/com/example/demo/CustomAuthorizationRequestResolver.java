package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

/**
 * Set the audience parameters for calling an API
 */
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final String audience;

    private final OAuth2AuthorizationRequestResolver defaultOauth2AuthorizationRequestResolver;

    @Autowired
    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                              String audience) {
        this.audience = audience;
        this.defaultOauth2AuthorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest resolve = this.defaultOauth2AuthorizationRequestResolver.resolve(request);
        return resolve != null ? customAuthorizationRequest(resolve) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest resolve = this.defaultOauth2AuthorizationRequestResolver.resolve(request, clientRegistrationId);
        return resolve != null ? customAuthorizationRequest(resolve) : null;
    }

    private OAuth2AuthorizationRequest customAuthorizationRequest(OAuth2AuthorizationRequest request) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>(request.getAdditionalParameters());
        params.put("audience", audience);
        params.put("prompt", "consent");

        OAuth2AuthorizationRequest appendedRequest =
                OAuth2AuthorizationRequest.from(request).additionalParameters(params).build();
        return appendedRequest;
    }
}
