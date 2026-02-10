package com.example.remarket.domain.auth.exception;

import com.example.remarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    // 토큰 관련 (JWT)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A-001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A-002", "만료된 토큰입니다."),
    NOT_EXISTS_TOKEN(HttpStatus.UNAUTHORIZED, "A-003", "토큰이 존재하지 않습니다."),
    NOT_EXISTS_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A-004", "리프레시 토큰이 존재하지 않습니다."),

    // 로그인/인증
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A-005", "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "A-006", "접근 권한이 없습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "A-007", "인증 코드가 올바르지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.REQUEST_TIMEOUT, "A-008", "코드가 만료되었습니다."),
    UNVERIFIED_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "A-009", "휴대폰 인증이 완료되지 않았습니다.");


    private HttpStatus status;
    private String code;
    private String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
