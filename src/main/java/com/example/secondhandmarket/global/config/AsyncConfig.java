package com.example.secondhandmarket.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 내부 로직용 스레드 풀 (조회수 증가, 이미지 처리 등)
     */
    @Bean(name = "taskExecutor") // 빈 이름을 "taskExecutor"로 하면 @Async만 써도 자동으로 인식됨
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);    // 평소 대기 스레드
        executor.setMaxPoolSize(20);    // 최대 스레드 (DB 부하 고려)
        executor.setQueueCapacity(50);  // 대기열
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    /**
     * 외부 API 호출용 스레드 풀 (SMS 발송 등)
     * 특징: 외부 응답 지연에 대비해 Max 사이즈를 넉넉하게 잡음
     */
    @Bean(name = "externalApiExecutor")
    public Executor externalApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ExtAPI-");
        executor.initialize();

        return executor;
    }

}
