package com.example.remarket.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class TestDatabaseConfig {

    @Container
    private static final MySQLContainer<?> mysqlContainer;

    static {
        // 실제 운영 환경과 동일한 버전의 MySQL 이미지 사용
        mysqlContainer = new MySQLContainer<>("mysql:8.0.33")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        mysqlContainer.start();

        // 컨테이너의 동적 JDBC URL 및 접속 정보를 시스템 프로퍼티에 주입
        System.setProperty("spring.datasource.url", mysqlContainer.getJdbcUrl() + "?rewriteBatchedStatements=true");
        System.setProperty("spring.datasource.username", mysqlContainer.getUsername());
        System.setProperty("spring.datasource.password", mysqlContainer.getPassword());
    }

    @Bean
    public MySQLContainer<?> mySQLContainer() {
        return mysqlContainer;
    }

    @PreDestroy
    public void stop() {
        if (mysqlContainer != null && mysqlContainer.isRunning()) {
            mysqlContainer.stop();
        }
    }
}
