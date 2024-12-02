package com.volco.creditapp.application.security;

import io.jsonwebtoken.Jwts;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtUtils {
    private static final Key KEY_SECRET = getSecretKey();
    private static final Map<String, String> JWT_HEADERS = Map.of("alg", "none");
    private static final long ONE_HOUR_IN_SECONDS = 3600;
    private static final long DEFAULT_EXPIRY_IN_SECONDS = 30;

    public static String createJwt(String username, List<String> roles, String customerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("roles", roles);
        if (customerId != null) {
            claims.put("customerId", customerId);
        }
        Instant currentTime = Instant.now();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(currentTime))
                .setExpiration(Date.from(currentTime.plusSeconds(ONE_HOUR_IN_SECONDS)))
                .setClaims(claims)
                .signWith(KEY_SECRET)
                .compact();
    }

    private static Key getSecretKey() {
        return Jwts.SIG.HS256.key().build();
    }

    public static Jwt buildJwtFromToken(String token) {
        return jwt(token);
    }

    // TODO check user from DB
    private static Jwt jwt(String token) {
        Map<String, Object> claims = extractClaims(token);
        Map<String, Object> headers = extractHeaders(token);
        return Jwt.withTokenValue(token)
                .expiresAt(Instant.now().plusSeconds(DEFAULT_EXPIRY_IN_SECONDS))
                .issuedAt(Instant.now())
                .headers(headersMap -> headersMap.putAll(headers))
                .claims(claimsMap -> claimsMap.putAll(claims))
                .build();
    }

    private static Map<String, Object> extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(KEY_SECRET)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static Map<String, Object> extractHeaders(String token) {
        try {
            return Jwts.parser()
                    .build()
                    .parseUnsecuredClaims(token)
                    .getHeader();
        } catch (Exception e) {
            return new HashMap<>(JWT_HEADERS);
        }
    }
}