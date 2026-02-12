package com.example.remarket.global.error;

import org.springframework.http.HttpStatus;

public enum GlobalErrorCode implements ErrorCode{

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G-001", "유효하지 않은 입력값입니다."),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G-002", "필수 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "G-003", "파라미터의 타입이 일치하지 않습니다."),

    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "G-004", "요청한 리소스를 찾을 수 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G-005", "지원하지 않는 HTTP 메서드입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G-006", "서버 내부 오류가 발생했습니다.");

    private HttpStatus status;
    private String code;
    private String message;

    GlobalErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() { return this.status; }

    @Override
    public String getCode() { return this.code; }

    @Override
    public String getMessage() { return this.message; }
}
