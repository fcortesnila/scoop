package com.scoop.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoop.api.dto.ApiDtos.ApiError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    JwtDecoder jwtDecoder(@Value("${app.google-client-id}") String clientId,
                          @Value("${app.demo-login-enabled:false}") boolean demoEnabled) {
        NimbusJwtDecoder googleDecoder = NimbusJwtDecoder.withIssuerLocation("https://accounts.google.com").build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer("https://accounts.google.com");
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                !clientId.isBlank() && jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid token audience", null));
        googleDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        if (!demoEnabled) return googleDecoder;

        return token -> {
            if (!token.equals("demo-operator")) return googleDecoder.decode(token);
            Instant now = Instant.now();
            return Jwt.withTokenValue(token).header("alg", "none")
                    .issuer("https://accounts.google.com").subject("demo-operator")
                    .audience(List.of(clientId.isBlank() ? "scoop-demo" : clientId))
                    .issuedAt(now).expiresAt(now.plusSeconds(86400))
                    .claim("email", "operator@example.com").claim("name", "Demo Operator")
                    .claim("email_verified", true).build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(objectMapper, response, HttpStatus.UNAUTHORIZED, "Authentication required"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(objectMapper, response, HttpStatus.FORBIDDEN, "Access denied")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/google", "/api/auth/demo", "/api/flavors", "/api/stops/current").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    private static void writeSecurityError(ObjectMapper objectMapper, HttpServletResponse response,
                                           HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiError(message, status.value(), LocalDateTime.now()));
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${app.operator-emails:}") String configuredOperatorEmails) {
        List<String> operatorEmails = List.of(configuredOperatorEmails.split(",")).stream()
                .map(String::trim).filter(email -> !email.isBlank()).toList();
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            String email = jwt.getClaimAsString("email");
            boolean emailVerified = Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"));
            if (email != null && emailVerified) {
                authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            }
            if (email != null && emailVerified && (operatorEmails.stream().anyMatch(email::equalsIgnoreCase)
                    || "demo-operator".equals(jwt.getSubject()))) {
                authorities.add(new SimpleGrantedAuthority("ROLE_OPERATOR"));
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors-origin}") String origin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(origin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
