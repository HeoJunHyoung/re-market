package com.example.secondhandmarket.global.security.jwt;

import com.example.secondhandmarket.domain.member.entity.enumerate.Role;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final String ROLE_KEY = "role";
    private final SecretKey key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public JwtTokenProvider(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.access_expiration_time}") long accessTokenValidityTime,
            @Value("${spring.jwt.refresh_expiration_time}") long refreshTokenValidityTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(AuthMember authMember) {
        return createToken(authMember, accessTokenValidityTime);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(AuthMember authMember) {
        return createToken(authMember, refreshTokenValidityTime);
    }

    private String createToken(AuthMember authMember, long validityTime) {

        long now = System.currentTimeMillis();
        Date validity = new Date(now + validityTime);

        return Jwts.builder()
                .subject(String.valueOf(authMember.getMemberId()))
                .claim(ROLE_KEY, authMember.getRole().name()) // 단일 Role
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * 토큰에서 인증 정보 조회
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Long memberId = Long.parseLong(claims.getSubject());
        String roleStr = claims.get(ROLE_KEY, String.class);
        Role role = Role.valueOf(roleStr);

        AuthMember authMember = new AuthMember(memberId, role);

        return new UsernamePasswordAuthenticationToken(
                authMember,
                null,
                authMember.getAuthorities()
        );
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


}
