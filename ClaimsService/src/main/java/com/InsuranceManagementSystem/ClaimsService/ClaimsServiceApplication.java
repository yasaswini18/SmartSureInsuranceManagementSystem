package com.InsuranceManagementSystem.ClaimsService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClaimsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimsServiceApplication.class, args);
		System.out.println("Claims Service Started Successfully\nPort: 8083\nInsurance Management System");
	}

}
