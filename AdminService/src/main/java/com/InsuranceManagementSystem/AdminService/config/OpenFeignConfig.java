package com.InsuranceManagementSystem.AdminService.config;

import feign.Client;
import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OpenFeignConfig {
	
	@Bean
	public feign.RequestInterceptor requestInterceptor() {
	    return requestTemplate -> {
	        org.springframework.web.context.request.ServletRequestAttributes attributes =
	            (org.springframework.web.context.request.ServletRequestAttributes)
	            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
	        if (attributes != null) {
	            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
	            String authHeader = request.getHeader("Authorization");
	            if (authHeader != null && !authHeader.isEmpty()) {
	                requestTemplate.header("Authorization", authHeader);
	            }
	        }
	    };
	}
	
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                3000, TimeUnit.MILLISECONDS,
                8000, TimeUnit.MILLISECONDS,
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
            String serviceName = "External service";
            if (methodKey.contains("Auth")) {
                serviceName = "Auth service";
            } else if (methodKey.contains("Policy")) {
                serviceName = "Policy service";
            } else if (methodKey.contains("Claims")) {
                serviceName = "Claims service";
            }

            if (response.status() == 400) {
                String body = "";
                try {
                    if (response.body() != null) {
                        body = new String(
                            response.body().asInputStream().readAllBytes(),
                            java.nio.charset.StandardCharsets.UTF_8
                        );
                        // Parse the message field out of the JSON body
                        com.fasterxml.jackson.databind.ObjectMapper mapper =
                            new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode node =
                            mapper.readTree(body);
                        if (node.has("message")) {
                            body = node.get("message").asText();
                        }
                    }
                } catch (Exception ignored) {}
                return new RuntimeException(body.isEmpty()
                    ? "Bad request to " + serviceName : body);
            }

            if (response.status() == 401) {
                return new RuntimeException(
                    "Unauthorized call to " + serviceName +
                    ". Admin token may have expired"
                );
            }
            if (response.status() == 403) {
                return new RuntimeException(
                    "Forbidden: " + serviceName +
                    " rejected the request"
                );
            }
            if (response.status() == 404) {
                return new RuntimeException(
                    "Resource not found in " + serviceName
                );
            }
            if (response.status() == 409) {
                return new RuntimeException(
                    "Conflict in " + serviceName +
                    ": Resource already exists"
                );
            }
            if (response.status() == -1 ||
                response.status() == 503) {
                return new RuntimeException(
                    serviceName + " is temporarily unavailable. " +
                    "Please try again later"
                );
            }
            return new RuntimeException(
                "Error calling " + serviceName +
                ": HTTP " + response.status()
            );
        };
    }
}