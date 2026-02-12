package com.example.remarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@EnableJpaAuditing
public class ReMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReMarketApplication.class, args);
    }

}
