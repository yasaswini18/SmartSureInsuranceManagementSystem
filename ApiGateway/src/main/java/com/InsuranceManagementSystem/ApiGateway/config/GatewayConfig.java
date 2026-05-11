package com.InsuranceManagementSystem.ApiGateway.config;

import com.InsuranceManagementSystem.ApiGateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for defining API Gateway routes.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Defines the routing rules for the microservices.
     *
     * @param builder The route locator builder.
     * @return The configured route locator.
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            .route("auth-service", r -> r
                .path("/api/auth/**")
                .uri("lb://AuthService")
            )

            .route("policy-service", r -> r
                .path("/api/policies/**")
                .filters(f -> f
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://PolicyService")
            )

            .route("claims-service", r -> r
                .path("/api/claims/**")
                .filters(f -> f
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://ClaimsService")
            )

            .route("admin-service", r -> r
                .path("/api/admin/**")
                .filters(f -> f
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://AdminService")
            )

            .route("auth-service-docs", r -> r
                .path("/auth-service/v3/api-docs")
                .filters(f -> f.rewritePath("/auth-service/v3/api-docs", "/v3/api-docs"))
                .uri("lb://AuthService")
            )

            .route("policy-service-docs", r -> r
                .path("/policy-service/v3/api-docs")
                .filters(f -> f.rewritePath("/policy-service/v3/api-docs", "/v3/api-docs"))
                .uri("lb://PolicyService")
            )

            .route("claims-service-docs", r -> r
                .path("/claims-service/v3/api-docs")
                .filters(f -> f.rewritePath("/claims-service/v3/api-docs", "/v3/api-docs"))
                .uri("lb://ClaimsService")
            )

            .route("admin-service-docs", r -> r
                .path("/admin-service/v3/api-docs")
                .filters(f -> f.rewritePath("/admin-service/v3/api-docs", "/v3/api-docs"))
                .uri("lb://AdminService")
            )

            .build();
    }
}