package com.omnicharge.rechargeprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RechargeProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RechargeProcessingApplication.class, args);
    }

}
