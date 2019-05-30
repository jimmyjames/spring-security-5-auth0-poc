package com.example.demo;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Controller
public class WebAppController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public WebAppController(RestTemplate restTemplate, OAuth2AuthorizedClientService authorizedClientService) {
        this.restTemplate = restTemplate;
        this.authorizedClientService = authorizedClientService;
    }

    @RequestMapping("/")
    public String getHomePage(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient authorizedClient = this.getAuthorizedClient(authentication);

        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());

        // Instead of calling userinfo endpoint, just get information from the authenticated principal (ID token)
        model.addAttribute("userinfo", authentication.getPrincipal().getAttributes());
        return "index";
    }

    @RequestMapping("/expenses")
    public String getExpenses(Model model, OAuth2AuthenticationToken authentication) {

        // call API - note the API is responsible for verifying the access token!
        ResponseEntity<String> result = restTemplate.exchange("http://localhost:3001/expenses", HttpMethod.GET,
                null, String.class);

        model.addAttribute("expenses", result.getBody());
        return "expenses";
    }

    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }
}