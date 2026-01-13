package com.example.secondhandmarket.domain.item.exception;

import com.example.secondhandmarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ItemErrorCode implements ErrorCode {
    ;

    private HttpStatus status;
    private String code;
    private String message;

}
