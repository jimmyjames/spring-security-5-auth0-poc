package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Controller
public class WebAppController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final WebClient webClient;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public WebAppController(WebClient webClient, OAuth2AuthorizedClientService authorizedClientService) {
        this.webClient = webClient;
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

        // MVC app needs to block, as thymeleaf only supports reactive data in WebFlux (non-MVC) environments
        // But WebClient will handle token refresh for us (!!)
        List<Expense> block = webClient.get()
                .uri("http://localhost:3001/expenses")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Expense.class)
                .collectList()
                .block();

        model.addAttribute("expenses", block);
        return "expenses";
    }

    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }
}