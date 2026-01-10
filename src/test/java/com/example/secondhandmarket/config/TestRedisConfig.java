package com.example.secondhandmarket.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class TestRedisConfig {

    @Container
    private static final GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>("redis:7.0")
                .withExposedPorts(6379);
        redisContainer.start();

        // 동적으로 할당된 호스트와 포트 번호를 Spring 설정에 주입
        String host = redisContainer.getHost();
        Integer port = redisContainer.getMappedPort(6379);

        System.setProperty("spring.data.redis.host", host);
        System.setProperty("spring.data.redis.port", port.toString());
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return redisContainer;
    }

    @PreDestroy
    public void stop() {
        if (redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }
}