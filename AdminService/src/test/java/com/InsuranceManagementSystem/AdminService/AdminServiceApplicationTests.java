package com.InsuranceManagementSystem.AdminService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=test-secret-key-for-testing-only-must-be-long-enough",
    "jwt.expiration=86400000",
    "eureka.client.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.import-check.enabled=false",
    "auth.service.url=localhost:8081",
    "claims.service.url=localhost:8082",
    "policy.service.url=localhost:8083"
})
class AdminServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}