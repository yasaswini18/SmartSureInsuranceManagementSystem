package com.InsuranceManagementSystem.ClaimsService.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Claims Service API")
                .description("""
                    Claims Service for Insurance Management System.
                    Handles claim submission, document upload,
                    claim tracking and admin review workflow.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Insurance Management System")
                    .email("admin@insurance.com")
                )
                .license(new License().name("Private"))
            )
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication")
            )
            .components(new Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Enter JWT token from Auth Service"
                        )
                )
            );
    }
}