package com.example.meuappjava.config;

import com.example.meuappjava.security.JwtRoleConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final Environment environment;
    private final JwtRoleConverter jwtRoleConverter;
    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;

    public SecurityConfig(
            Environment environment,
            JwtRoleConverter jwtRoleConverter,
            ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        this.environment = environment;
        this.jwtRoleConverter = jwtRoleConverter;
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,
                                "/actuator/health", "/actuator/health/**",
                                "/actuator/metrics", "/actuator/metrics/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/swagger-ui.html",
                                "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)));

        String issuerUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        String jwkSetUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        if (StringUtils.hasText(issuerUri)
                || StringUtils.hasText(jwkSetUri)
                || jwtDecoderProvider.getIfAvailable() != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtRoleConverter)));
        } else {
            http.httpBasic(Customizer.withDefaults());
        }

        return http.build();
    }
}
