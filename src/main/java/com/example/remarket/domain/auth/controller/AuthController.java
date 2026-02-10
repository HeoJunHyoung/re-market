package com.example.remarket.domain.auth.controller;

import com.example.remarket.domain.auth.dto.response.AuthResult;
import com.example.remarket.domain.auth.dto.response.LoginResponse;
import com.example.remarket.domain.auth.service.AuthService;
import com.example.remarket.global.util.CookieUtil;
import com.example.remarket.domain.auth.dto.request.JoinRequest;
import com.example.remarket.domain.auth.dto.request.LoginRequest;
import com.example.remarket.domain.auth.dto.request.SendCodeRequest;
import com.example.remarket.domain.auth.dto.request.VerifyRequest;
import com.example.remarket.global.security.principal.AuthMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestBody SendCodeRequest request) {
        authService.sendCode(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@RequestBody VerifyRequest request) {
        authService.verifyCode(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody JoinRequest request) {
        authService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResult authResult = authService.login(request);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(authResult.getAccessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(authResult.getRefreshToken()).toString());

        return ResponseEntity.ok(authResult.getLoginResponse()); // LoginResponse 반환 이유: Role에 따라 페이지 이동 분기처리를 위해
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(@AuthenticationPrincipal AuthMember authMember, HttpServletResponse response) {
        authService.logout(authMember.getMemberId());

        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createExpiredCookie("accessToken").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createExpiredCookie("refreshToken").toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.resolveToken(request, "refreshToken");

        AuthResult authResult = authService.reissue(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(authResult.getAccessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(authResult.getRefreshToken()).toString());

        return ResponseEntity.ok().build();
    }

}
