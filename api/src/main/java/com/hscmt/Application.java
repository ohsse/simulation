package com.hscmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.hscmt", "com.hscmt.simulation", "com.hscmt.waternet"},exclude = { DataSourceAutoConfiguration.class })
@EnableJpaAuditing
@EnableCaching
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
