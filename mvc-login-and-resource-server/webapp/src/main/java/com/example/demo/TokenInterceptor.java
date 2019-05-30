package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * Intercept requests and add the Bearer token header. If the access token is expired, will use the refresh token
 * to get a new one.
 */
public class TokenInterceptor implements ClientHttpRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final OAuth2AuthorizedClientService authorizedClientService;

    TokenInterceptor(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient authorizedClient = this.getAuthorizedClient((OAuth2AuthenticationToken)authentication);

        // returns access token with both userinfo and API audience - can be used to call API
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        if (isExpired(accessToken)) {
            log.debug("Access token is expired, will attempt to refresh");
            refreshToken(authorizedClient, (OAuth2AuthenticationToken)authentication);
        }

        request.getHeaders().add("Authorization", "Bearer " + accessToken.getTokenValue());


        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);

        return response;
    }

    private boolean isExpired(OAuth2AccessToken accessToken) {
        Instant expiresAt = accessToken.getExpiresAt();
        Instant now = Instant.now();

        return now.isAfter(expiresAt.minus(Duration.ofSeconds(10)));
    }

    private void refreshToken(OAuth2AuthorizedClient authorizedClient, OAuth2AuthenticationToken currentUser) {

        OAuth2AccessTokenResponse tokenResponse = refreshTokenClient(authorizedClient);
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            log.error("Error refreshing token");
            return;
        }

        OAuth2RefreshToken refreshToken = tokenResponse.getRefreshToken() == null ?
                authorizedClient.getRefreshToken() : tokenResponse.getRefreshToken();

        OAuth2AuthorizedClient newClient = new OAuth2AuthorizedClient(authorizedClient.getClientRegistration(),
                authorizedClient.getPrincipalName(), tokenResponse.getAccessToken(), refreshToken);

        this.authorizedClientService.saveAuthorizedClient(newClient, currentUser);
    }

    private OAuth2AccessTokenResponse refreshTokenClient(OAuth2AuthorizedClient authorizedClient) {
        LinkedMultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.REFRESH_TOKEN.getValue());
        formParams.add(OAuth2ParameterNames.REFRESH_TOKEN, authorizedClient.getRefreshToken().getTokenValue());
        formParams.add(OAuth2ParameterNames.REDIRECT_URI, authorizedClient.getClientRegistration().getRedirectUriTemplate());

        RequestEntity<LinkedMultiValueMap<String, String>> requestEntity = RequestEntity
                .post(URI.create(authorizedClient.getClientRegistration().getProviderDetails().getTokenUri()))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .body(formParams);

        // TODO error handling
        RestTemplate restTemplate = restTemplate(authorizedClient.getClientRegistration().getClientId(),
                authorizedClient.getClientRegistration().getClientSecret());
        ResponseEntity<OAuth2AccessTokenResponse> response = restTemplate.exchange(requestEntity, OAuth2AccessTokenResponse.class);
        return response.getBody();

    }

    private RestTemplate restTemplate(String clientId, String clientSecret) {
        return new RestTemplateBuilder()
                .additionalMessageConverters(
                        new FormHttpMessageConverter(),
                        new OAuth2AccessTokenResponseHttpMessageConverter())
                .errorHandler(new OAuth2ErrorResponseErrorHandler())
                .basicAuthentication(clientId, clientSecret)
                .build();
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        log.debug("===========================request begin================================================");
        log.debug("URI         : {}", request.getURI());
        log.debug("Method      : {}", request.getMethod());
        log.debug("Headers     : {}", request.getHeaders());
        log.debug("Request body: {}", new String(body, "UTF-8"));
        log.debug("==========================request end================================================");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        log.debug("============================response begin==========================================");
        log.debug("Status code  : {}", response.getStatusCode());
        log.debug("Status text  : {}", response.getStatusText());
        log.debug("Headers      : {}", response.getHeaders());
        log.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
        log.debug("=======================response end=================================================");
    }

    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }
}
