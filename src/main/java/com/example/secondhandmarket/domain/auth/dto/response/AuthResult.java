package com.example.secondhandmarket.domain.auth.dto.response;

import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResult {

    private String accessToken;

    private String refreshToken;

    private LoginResponse loginResponse;

    public static AuthResult from(String accessToken, String refreshToken, AuthMember authMember) {
        return AuthResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .loginResponse(LoginResponse.from(authMember))
                .build();
    }

}
