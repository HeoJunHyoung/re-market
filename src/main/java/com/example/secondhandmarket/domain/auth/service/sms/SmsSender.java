package com.example.secondhandmarket.domain.auth.service.sms;

public interface SmsSender {
    void send(String phoneNumber, String message);
}
