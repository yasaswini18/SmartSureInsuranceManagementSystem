package com.InsuranceManagementSystem.PolicyService.config;

import com.InsuranceManagementSystem.PolicyService.security.CustomAccessDeniedHandler;
import com.InsuranceManagementSystem.PolicyService.security.CustomAuthenticationEntryPoint;
import com.InsuranceManagementSystem.PolicyService.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )

            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(
                	        "/swagger-ui/**",
                	        "/swagger-ui.html",
                	        "/v3/api-docs/**",
                	        "/v3/api-docs",
                	        "/swagger-resources/**",
                	        "/webjars/**"
                	    ).permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/policies/products",
                        "/api/policies/products/{id}",
                        "/api/policies/products/type/**"
                ).permitAll()

                .requestMatchers(HttpMethod.POST,
                        "/api/policies/products"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT,
                        "/api/policies/products/{id}"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE,
                        "/api/policies/products/{id}"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.PATCH,
                        "/api/policies/products/{id}/reactivate"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,
                        "/api/policies/all"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,
                        "/api/policies/purchase"
                ).hasRole("USER")

                .requestMatchers(HttpMethod.GET,
                        "/api/policies/my-policies"
                ).hasRole("USER")

                .requestMatchers(HttpMethod.PUT,
                        "/api/policies/{policyId}/cancel"
                ).hasRole("USER")

                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );
          

        return http.build();
    }
}