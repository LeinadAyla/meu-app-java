package com.example.meuappjava.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.util.Assert;

@Configuration
@Profile("demo")
public class DemoAuthConfiguration {

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService demoUserDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${audita.demo.passwords.ingestor}") String ingestorPassword,
            @Value("${audita.demo.passwords.auditor}") String auditorPassword,
            @Value("${audita.demo.passwords.admin}") String adminPassword) {
        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(
                User.withUsername("ingestor")
                        .password(passwordEncoder.encode(ingestorPassword))
                        .roles("INGESTOR")
                        .build(),
                User.withUsername("auditor")
                        .password(passwordEncoder.encode(auditorPassword))
                        .roles("AUDITOR")
                        .build(),
                User.withUsername("admin")
                        .password(passwordEncoder.encode(adminPassword))
                        .roles("ADMIN", "INGESTOR", "AUDITOR")
                        .build());
    }

    @Bean
    SecretKey demoJwtSecretKey(@Value("${audita.demo.jwt-secret}") String secret) {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        Assert.isTrue(key.length >= 32, "audita.demo.jwt-secret deve ter pelo menos 32 bytes");
        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey demoJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(demoJwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey demoJwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(demoJwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("auditagov-demo"));
        return decoder;
    }
}
