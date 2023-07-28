package com.imcloud.saas_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SaasUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasUserApplication.class, args);
    }

}
