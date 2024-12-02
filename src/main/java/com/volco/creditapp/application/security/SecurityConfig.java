package com.volco.creditapp.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/token/**",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers(toH2Console()).permitAll()
                        .requestMatchers("/customers/{customerId}/loans").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().authenticated() // Protect all other endpoints
                ).headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {
                            jwt.decoder(new CustomJwtDecoder());
                            jwt.jwtAuthenticationConverter(new CustomJwtConverter());
                        }
                ));
        return http.build();
    }

    private class CustomJwtDecoder implements JwtDecoder {
        @Override
        public Jwt decode(String token) throws JwtException {
            return JwtUtils.buildJwtFromToken(token);
        }
    }
}