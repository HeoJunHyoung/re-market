package com.example.remarket.domain.trade.exception;

import com.example.remarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TradeErrorCode implements ErrorCode {

    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "T-001", "존재하지 않는 거래입니다.");

    private HttpStatus status;
    private String code;
    private String message;

    TradeErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
