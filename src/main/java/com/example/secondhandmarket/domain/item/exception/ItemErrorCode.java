package com.example.secondhandmarket.domain.item.exception;

import com.example.secondhandmarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ItemErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I-001", "존재하지 않는 상품입니다.");



    private HttpStatus status;
    private String code;
    private String message;

    ItemErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
