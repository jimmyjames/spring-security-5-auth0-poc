package com.auth0.example;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value( "${auth0.audience}" )
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/api/public").permitAll()

                // TODO expression language not supported? Stopped working when adding additional validation...
                // .antMatchers("/api/private-scoped").access("#oauth2.hasScope('read:messages')")

                .mvcMatchers("api/private-scoped").hasAuthority("SCOPE_read:messages")
                .mvcMatchers("/api/private").authenticated()
                .and()
                .oauth2ResourceServer().jwt();
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
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoderJwkSupport jwtDecoder = (NimbusJwtDecoderJwkSupport)
                JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
}