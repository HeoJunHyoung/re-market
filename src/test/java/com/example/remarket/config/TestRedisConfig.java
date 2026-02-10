package com.example.remarket.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRedisConfig {

    private static final String REDIS_IMAGE = "redis:7.0";
    private static final int REDIS_PORT = 6379;
    private static final String TEST_PASSWORD = "test_password"; // 테스트용 임시 비밀번호

    private GenericContainer<?> redisContainer;

    @PostConstruct
    public void startRedis() {
        try {
            // 1. Redis 컨테이너를 띄울 때 '--requirepass' 옵션으로 비밀번호를 강제 설정합니다.
            redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                    .withExposedPorts(REDIS_PORT)
                    .withCommand("redis-server", "--requirepass", TEST_PASSWORD);

            redisContainer.start();

            // 2. Spring Boot(Redisson)가 이 컨테이너에 접속할 때 사용할 정보를 System Property로 주입합니다.
            // (System Property는 application.yml보다 우선순위가 높습니다)
            System.setProperty("spring.data.redis.host", redisContainer.getHost());
            System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(REDIS_PORT).toString());
            System.setProperty("spring.data.redis.password", TEST_PASSWORD);

        } catch (Exception e) {
            throw new RuntimeException("Redis Container failed to start", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisContainer != null) {
            redisContainer.stop();
        }
        // 테스트 종료 후 시스템 속성 정리 (선택 사항)
        System.clearProperty("spring.data.redis.host");
        System.clearProperty("spring.data.redis.port");
        System.clearProperty("spring.data.redis.password");
    }
}