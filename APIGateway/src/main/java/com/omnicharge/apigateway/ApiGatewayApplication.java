package com.omnicharge.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class ApiGatewayApplication {

    public static void main(String[] args) {
        log.info("Starting API Gateway application...");
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("API Gateway application started successfully");
    }

}
