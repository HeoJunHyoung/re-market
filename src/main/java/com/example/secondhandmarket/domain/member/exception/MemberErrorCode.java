package com.example.secondhandmarket.domain.member.exception;

import com.example.secondhandmarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {

    // 회원 조회
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M-001", "존재하지 않는 회원입니다."),

    // 회원가입
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "M-002", "이미 존재하는 회원 아이디입니다."),

    // 입력값 검증 (비밀번호 확인 불일치 등)
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "M-003", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    private MemberErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
