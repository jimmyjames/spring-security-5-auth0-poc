package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Configure RestTemplate with an interceptor to add the Bearer token.
 */
@Configuration
public class RestTemplateConfig {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public RestTemplateConfig(OAuth2AuthorizedClientService auth2AuthorizedClientService) {
        this.authorizedClientService = auth2AuthorizedClientService;
    }

    @Bean
    public RestTemplate restTemplate() {
        // BufferingClientHttpRequestFactory needed so we can log the response without it getting destoryed
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setInterceptors(Collections.singletonList(new TokenInterceptor(authorizedClientService)));
        return restTemplate;
    }
}
