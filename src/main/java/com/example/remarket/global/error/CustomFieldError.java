package com.example.remarket.global.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomFieldError {

    private String field;
    private String value;
    private String reason;

    private CustomFieldError(String field, String value, String reason) {
        this.field = field;
        this.value = value;
        this.reason = reason;
    }

    public static List<CustomFieldError> of(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.stream()
                .map(error -> new CustomFieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());
    }

}
