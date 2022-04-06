package com.x0.sbootplayground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SpringBootSamplesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSamplesApplication.class, args);
    }

}
