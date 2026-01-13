package com.example.secondhandmarket.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyRequest {
    String code;
    String phoneNumber;
}
