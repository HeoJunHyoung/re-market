package com.example.secondhandmarket.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 로직 실행 중 발생하는 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // 바인딩 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INVALID_INPUT_VALUE, bindingResult);
        return new ResponseEntity<>(response, GlobalErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // 나머지 모든 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Internal Server Error: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }


}
