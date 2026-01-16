package com.example.secondhandmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class SecondhandMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandMarketApplication.class, args);
    }

}
