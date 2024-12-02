package com.volco.creditapp.application.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                source.getTokenValue(),
                source.getIssuedAt(),
                source.getExpiresAt()
        );
        Map<String, Object> attributes = getAttributes(source);
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter.convert(source);

        OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(attributes, authorities);
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    private Map<String, Object> getAttributes(Jwt jwt) {
        return new HashMap<>(jwt.getClaims().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("roles"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static class JwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt source) {
            List<String> authorities = getAuthorities(source);
            return authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        private List<String> getAuthorities(Jwt jwt) {
            List<String> roles = (List<String>)jwt.getClaims().get("roles");
            return Objects.requireNonNullElse(roles, Collections.emptyList());
        }
    }
}