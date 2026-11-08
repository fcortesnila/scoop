package com.scoop.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScoopApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScoopApiApplication.class, args);
    }
}
