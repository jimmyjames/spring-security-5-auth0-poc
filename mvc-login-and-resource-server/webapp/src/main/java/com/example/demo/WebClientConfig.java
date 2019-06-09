package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient(ClientRegistrationRepository clientRegistrations,
                        OAuth2AuthorizedClientRepository authorizedClients) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);
        // explicitly opt into using the oauth2Login to provide an access token implicitly
        oauth.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .build();
    }

}
