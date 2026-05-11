package com.InsuranceManagementSystem.ApiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the API Gateway service.
 * Handles routing and security filtering for all incoming requests.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    /**
     * Main method to start the API Gateway service.
     *
     * @param args Command line arguments.
     */
	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
