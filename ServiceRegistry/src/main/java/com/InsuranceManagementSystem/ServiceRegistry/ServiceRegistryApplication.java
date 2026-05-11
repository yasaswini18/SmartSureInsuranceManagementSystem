package com.InsuranceManagementSystem.ServiceRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Main application class for the Service Registry.
 * Uses Eureka Server to register and discover microservices.
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    /**
     * Main method to start the Service Registry application.
     *
     * @param args Command line arguments.
     */
	public static void main(String[] args) {
		SpringApplication.run(ServiceRegistryApplication.class, args);
	}

}
