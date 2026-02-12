package com.example.remarket.global.error;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String code;
    private String message;
    private List<CustomFieldError> errors;

    protected ErrorResponse() {}

    public ErrorResponse(ErrorCode errorCode, List<CustomFieldError> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = errorCode.getStatus().value();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }

    // 일반 에러 응답 (Validation 에러 없음)
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode, new ArrayList<>());
    }

    // Validation 에러 응답 (BindingResult 포함)
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(errorCode, CustomFieldError.of(bindingResult));
    }

}

