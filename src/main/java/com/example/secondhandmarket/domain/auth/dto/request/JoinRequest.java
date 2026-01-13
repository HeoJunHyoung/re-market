package com.example.secondhandmarket.domain.auth.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinRequest {
    private String username;
    private String password;
    private String passwordConfirm;
    private String phoneNumber;
}
