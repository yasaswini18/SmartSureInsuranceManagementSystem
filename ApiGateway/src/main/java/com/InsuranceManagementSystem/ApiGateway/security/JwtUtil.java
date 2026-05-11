package com.InsuranceManagementSystem.ApiGateway.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for JSON Web Token (JWT) operations.
 * Handles extracting claims and validating tokens.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts a specific claim from the token.
     *
     * @param token          The JWT token.
     * @param claimsResolver The function to extract the claim.
     * @param <T>            The type of the claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the email (subject) from the token.
     *
     * @param token The JWT token.
     * @return The extracted email.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the role from the token.
     *
     * @param token The JWT token.
     * @return The extracted role.
     */
    public String extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get("role", String.class));
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token The JWT token.
     * @return The extracted expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validates the given JWT token.
     *
     * @param token The JWT token.
     * @return True if the token is valid and not expired, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}