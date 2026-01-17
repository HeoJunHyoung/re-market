package com.example.secondhandmarket.domain.review.exception;


import com.example.secondhandmarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReviewErrorCode implements ErrorCode {

    ALREADY_EXISTS_REVIEW(HttpStatus.NOT_FOUND, "R-001", "이미 후기를 작성했습니다.");

    private HttpStatus status;
    private String code;
    private String message;

    ReviewErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}