package com.example.remarket.domain.auth.service.sms;

public interface SmsSender {
    void send(String phoneNumber, String message);
}
