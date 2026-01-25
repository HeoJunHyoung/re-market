package com.example.secondhandmarket.domain.item.exception;

import com.example.secondhandmarket.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ItemErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I-001", "존재하지 않는 상품입니다."),
    NOT_ITEM_OWNER(HttpStatus.FORBIDDEN, "I-002", "상품 수정 권한이 없습니다."),
    BUYER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "I-003", "구매자 정보가 필요합니다."),
    ALREADY_SOLD_OUT(HttpStatus.BAD_REQUEST, "I-004", "이미 판매 완료된 상품입니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "I-005", "재고가 부족합니다."),
    INVALID_ITEM_TYPE(HttpStatus.BAD_REQUEST, "I-006", "나눔 상품만 신청 가능합니다."),
    CANNOT_BUY_MY_ITEM(HttpStatus.BAD_REQUEST, "I-007", "자신의 나눔 상품에는 나눔 신청이 불가능합니다."),
    ALREADY_REQUESTED(HttpStatus.BAD_REQUEST, "I-008", "이미 신청하셨습니다.");

    private HttpStatus status;
    private String code;
    private String message;

    ItemErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
