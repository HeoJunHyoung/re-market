package com.example.remarket.domain.auth.service.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncSmsSender {

    private final SmsSender smsSender;

    /**
     * 비동기 SMS 발송 메서드
     * - @Async("externalApiExecutor"): 위에서 만든 스레드 풀을 사용하여 실행
     * - 호출한 쪽(AuthService)은 즉시 리턴받고, 실제 작업은 별도 스레드에서 진행됨
     */
    @Async("externalApiExecutor")
    public void send(String phoneNumber, String message) {
        log.info("[Async] 비동기 SMS 발송 요청 시작 (별도 스레드에서 실행)");
        smsSender.send(phoneNumber, message); // 여기서 2초 지연 발생 (하지만 메인 스레드는 영향 없음)
        log.info("[Async] 비동기 SMS 발송 완료");
    }
}