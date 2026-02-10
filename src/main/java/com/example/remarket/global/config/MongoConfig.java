package com.example.remarket.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.example.remarket.domain.**.repository.mongo"
)
public class MongoConfig {
}
