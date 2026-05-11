package com.InsuranceManagementSystem.PolicyService.config;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OpenFeignConfig {

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                3000, TimeUnit.MILLISECONDS,
                5000, TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {

            if (response.status() == 401) {
                return new RuntimeException("Unauthorized: Invalid or expired token");
            }

            if (response.status() == 403) {
                return new RuntimeException("Forbidden: Insufficient permissions");
            }

            if (response.status() == 404) {
                return new RuntimeException("User not found in auth service");
            }
            if (response.status() == 409) {                              
                return new RuntimeException("Conflict: Resource already exists");
            }
            if (response.status() == 500) {
                return new RuntimeException("Auth service error. Please try again later");
            }

            return new RuntimeException("Error calling auth service: " + response.status());
        };
    }
}