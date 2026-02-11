package com.example.remarket.domain.region.exception;

import com.example.remarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RegionErrorCode implements ErrorCode {

    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "R-001", "존재하지 않는 상품입니다.");

    private HttpStatus status;
    private String code;
    private String message;

    RegionErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
