package com.github.defsignals.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class ServiceApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceApplication.class)
                .sources()
                .run(args);
    }
}
