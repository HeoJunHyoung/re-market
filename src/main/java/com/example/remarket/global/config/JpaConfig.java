package com.example.remarket.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.remarket.domain.**.repository.jpa"
)
@EntityScan("com.example.remarket.domain")
public class JpaConfig {
}
