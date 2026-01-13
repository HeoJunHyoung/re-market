package com.example.secondhandmarket.global.security.jwt;

import com.example.secondhandmarket.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 쿠키에서 토큰 추출
        String token = cookieUtil.resolveToken(request, "accessToken");

        // 2. 토큰 유효성 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 인증 객체 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // 4. SecurityContext에 저장 (로그인 처리 완료)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 다음 필터로 진행 (토큰이 없거나 유효하지 않아도 일단 통과시킨다. 판단은 뒤의 필터들이 수행함)
        filterChain.doFilter(request, response);
    }

}
