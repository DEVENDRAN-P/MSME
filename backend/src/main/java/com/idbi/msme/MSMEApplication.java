package com.idbi.msme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MSMEApplication {
    public static void main(String[] args) {
        SpringApplication.run(MSMEApplication.class, args);
    }
}
