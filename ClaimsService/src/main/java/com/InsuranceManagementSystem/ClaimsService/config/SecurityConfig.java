package com.InsuranceManagementSystem.ClaimsService.config;

import com.InsuranceManagementSystem.ClaimsService.security.JwtAuthFilter;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(
                	        "/swagger-ui/**",
                	        "/swagger-ui.html",
                	        "/v3/api-docs/**",
                	        "/v3/api-docs",
                	        "/swagger-resources/**",
                	        "/webjars/**"
                	    ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/claims/initiate").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/claims/{claimId}/documents").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/claims/my-claims").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/claims/status/{claimId}").hasRole("USER")

                .requestMatchers(HttpMethod.GET, "/api/claims/{claimId}/documents").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/claims/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/claims/pending").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/claims/{claimId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/claims/documents/{documentId}/download").authenticated()

                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            )
        .exceptionHandling(ex -> ex
        	    .authenticationEntryPoint((request, response, authException) -> {
        	        System.out.println("AUTH FAILED: " + authException.getMessage()); // ← add
        	        response.sendError(403, authException.getMessage());
        	    })
        	    );
      
        return http.build();
    }
}