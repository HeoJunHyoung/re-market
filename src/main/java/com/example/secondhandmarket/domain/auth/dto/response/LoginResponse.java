package com.example.secondhandmarket.domain.auth.dto.response;

import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String memberId;

    private String role;

    public static LoginResponse from(AuthMember authMember) {
        return LoginResponse.builder()
                .memberId(String.valueOf(authMember.getMemberId()))
                .role(String.valueOf(authMember.getRole()))
                .build();
    }

}
