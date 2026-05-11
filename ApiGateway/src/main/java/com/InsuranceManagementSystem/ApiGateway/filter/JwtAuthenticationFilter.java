package com.InsuranceManagementSystem.ApiGateway.filter;

import com.InsuranceManagementSystem.ApiGateway.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Authentication filter that intercepts incoming requests to validate JWT tokens.
 * Enforces security by checking for valid tokens on protected routes and
 * ensuring admin-only access for admin routes.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends
        AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Applies the authentication filter logic to the request.
     *
     * @param config The filter configuration.
     * @return The configured gateway filter.
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.info("Gateway → {} {}", request.getMethod(), path);

            if (isPublicRoute(path)) {
                log.info("Public route → forwarding: {}", path);
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("No auth header → {}", path);
                return onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid auth header → {}", path);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token → {}", path);
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            log.info("Authenticated → email: {}, role: {}, path: {}", email, role, path);

            if (isAdminRoute(path) && !role.equals("ADMIN")) {
                log.warn("Non-admin access denied → {}", path);
                return onError(exchange, "Admin access required", HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest modifiedRequest = request
                    .mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(modifiedRequest)
                            .build()
            );
        };
    }

    private boolean isPublicRoute(String path) {
        if (path.equals("/api/auth/login") ||
            path.equals("/api/auth/register") ||
            path.equals("/api/auth/health")) {
            return true;
        }

        if (path.equals("/api/policies/products") ||
            path.matches("/api/policies/products/\\d+") ||
            path.matches("/api/policies/products/type/.*")||
            path.equals("/api/policies/health")){
            return true;
        }
        
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.endsWith("/v3/api-docs") ||
        path.startsWith("/auth-service/v3/api-docs") ||
        path.startsWith("/policy-service/v3/api-docs") ||
        path.startsWith("/claims-service/v3/api-docs") ||
        path.startsWith("/admin-service/v3/api-docs")) {
                return true;
            }
        if (path.startsWith("/actuator")) {
            return true;
        }
        return false;
    }

    private boolean isAdminRoute(String path) {
        return path.startsWith("/api/admin/");
    }

    private Mono<Void> onError(
            ServerWebExchange exchange,
            String message,
            HttpStatus status
    ) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"status\":%d,\"message\":\"%s\"}",
                status.value(), message
        );

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    public static class Config {
    }
}