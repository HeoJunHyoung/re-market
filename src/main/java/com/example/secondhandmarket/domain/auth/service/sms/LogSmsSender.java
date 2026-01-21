package com.example.secondhandmarket.domain.auth.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogSmsSender implements SmsSender{

    @Override
    public void send(String phoneNumber, String message) {
        // 실제 발송 대신 로그만 출력
        log.info("[SMS 발송] 번호: {}, 인증코드: {}", phoneNumber, message);
    }

}
