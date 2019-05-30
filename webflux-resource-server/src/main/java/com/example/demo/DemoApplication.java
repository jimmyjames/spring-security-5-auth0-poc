package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveMethodSecurity
@Controller
public class DemoApplication {

	@Value( "${auth0.audience}" )
	private String audience;

	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String issuer;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@EnableWebFluxSecurity
	static class SecurityConfig {

		@Bean
		public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
			return http
					.authorizeExchange()
					.pathMatchers("/api/public").permitAll()
					.pathMatchers("/api/private").authenticated()
					.pathMatchers("/api/private-scoped").hasAuthority("SCOPE_read:messages")
					.and()
					.oauth2ResourceServer()
					.jwt().and().and().build();
		}
	}

	/**
	 * By default, Spring will not validate audience, so we need to do that.
	 * (It will verify signature (DefaultJWTProcessor.process)
	 */
	class AudienceValidator implements OAuth2TokenValidator<Jwt> {
		OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);

		public OAuth2TokenValidatorResult validate(Jwt jwt) {
			if (jwt.getAudience().contains(audience)) {
				return OAuth2TokenValidatorResult.success();
			} else {
				return OAuth2TokenValidatorResult.failure(error);
			}
		}
	}

	@Bean
	ReactiveJwtDecoder jwtDecoder() {
		NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder)
				ReactiveJwtDecoders.fromOidcIssuerLocation(issuer);

		OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator();
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
		OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator,
				new JwtTimestampValidator());

		jwtDecoder.setJwtValidator(withAudience);

		return jwtDecoder;
	}

	@RequestMapping(value = "/api/public", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Mono<String> publicEndpoint() {
		return Mono.just("All good. You DO NOT need to be authenticated to call this API.");
	}

	@GetMapping(value = "/api/private", produces = "application/json")
	@ResponseBody
	public Mono<String> privateEndpoint() {
		return Mono.just( "All good. You can see this because you are Authenticated.");
	}

	@GetMapping(value = "api/private-scoped", produces = "application/json")
	@ResponseBody
	public Mono<String> privateScopedEndpoint() {
		return Mono.just("All good. You can see this because you are Authenticated with a Token granted the 'read:messages' scope");
	}
}


