
package com.jvproject.club_management_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final String TOKEN_TYPE = "tokenType";
    private static final String USER_ID = "userId";
    private static final String ROLES = "roles";

    private static final String ACCESS = "ACCESS";
    private static final String REFRESH = "REFRESH";

    private final SecretKey secretKey;
    @Getter
    private final long accessExpiration;
    @Getter
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Generate an access token containing user ID, email, and roles.
    public String generateAccessToken(
            Long userId,
            String email,
            List<String> roles
    ) {
        return generateToken(
                userId,
                email,
                roles,
                ACCESS,
                accessExpiration
        );
    }

    // Generate a refresh token with a separate token type.
    public String generateRefreshToken(String email) {
        return generateToken(
                null,
                email,
                List.of(),
                REFRESH,
                refreshExpiration
        );
    }

    private String generateToken(
            Long userId,
            String email,
            List<String> roles,
            String tokenType,
            long expiration
    ) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiresAt);

        if (ACCESS.equals(tokenType)) {
            builder.claim(USER_ID, userId);
            builder.claim(ROLES, roles);
        }

        return builder
                .signWith(secretKey)
                .compact();
    }

    // Extract email after verifying the token signature and expiration.
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Extract user ID from a valid access token.
    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);

        requireTokenType(claims, ACCESS);

        return claims.get(USER_ID, Long.class);
    }

    // Extract roles from a valid access token.
    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);

        requireTokenType(claims, ACCESS);

        List<?> values = claims.get(ROLES, List.class);

        if (values == null ||
                values.stream().anyMatch(value -> !(value instanceof String))) {
            throw new JwtException("Invalid roles claim");
        }

        return values.stream()
                .map(String.class::cast)
                .toList();
    }

    // Validate an access token for the expected email.
    public boolean isAccessTokenValid(
            String token,
            String email
    ) {
        return isTokenValid(token, email, ACCESS);
    }

    // Validate a refresh token for the expected email.
    public boolean isRefreshTokenValid(
            String token,
            String email
    ) {
        return isTokenValid(token, email, REFRESH);
    }

    private boolean isTokenValid(
            String token,
            String email,
            String expectedType
    ) {
        try {
            Claims claims = parseClaims(token);

            return email != null
                    && email.equals(claims.getSubject())
                    && expectedType.equals(
                    claims.get(TOKEN_TYPE, String.class)
            );

        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private void requireTokenType(
            Claims claims,
            String expectedType
    ) {
        if (!expectedType.equals(
                claims.get(TOKEN_TYPE, String.class)
        )) {
            throw new JwtException("Invalid token type");
        }
    }

    // Verifies the signature and checks expiration during parsing.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
