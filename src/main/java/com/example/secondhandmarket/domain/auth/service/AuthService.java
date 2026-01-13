package com.example.secondhandmarket.domain.auth.service;

import com.example.secondhandmarket.domain.auth.dto.request.JoinRequest;
import com.example.secondhandmarket.domain.auth.dto.request.LoginRequest;
import com.example.secondhandmarket.domain.auth.dto.request.SendCodeRequest;
import com.example.secondhandmarket.domain.auth.dto.request.VerifyRequest;
import com.example.secondhandmarket.domain.auth.dto.response.AuthResult;
import com.example.secondhandmarket.domain.auth.exception.AuthErrorCode;
import com.example.secondhandmarket.domain.auth.service.sms.SmsSender;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.exception.MemberErrorCode;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import com.example.secondhandmarket.global.error.BusinessException;
import com.example.secondhandmarket.global.error.GlobalErrorCode;
import com.example.secondhandmarket.global.security.jwt.JwtTokenProvider;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final SmsSender smsSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String NICKNAME_PREFIX = "사용자#";
    private static final String SMS_AUTH_PREFIX = "sms:auth:";
    private static final String SMS_VERIFIED_PREFIX = "sms:verified:";
    private static final String NICKNAME_SEQ_KEY = "nickname:sequence";

    /**
     * SMS 인증 코드 전송
     */
    @Transactional
    public void sendCode(SendCodeRequest request) {
        // 1. 인증 코드 생성
        String code = generateVerifyCode();

        // 2. Redis 저장 (3분 TTL)
        redisTemplate.opsForValue()
                .set(SMS_AUTH_PREFIX+ request.getPhoneNumber(), code, Duration.ofMinutes(3));

        // 3. 발송 (현재는 Dev 환경이라서 단순 Log 출력)
        smsSender.send(request.getPhoneNumber(), code);
    }

    /**
     * SMS 인증 코드 검사
     */
    @Transactional
    public void verifyCode(VerifyRequest request) {
        String redisKey = SMS_AUTH_PREFIX + request.getPhoneNumber();

        // 1. 인증 요청 사용자의 전화번호를 통해 서버가 저장하고 있는 인증 코드 조회
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BusinessException(AuthErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        // 2. 인증 요청 사용자가 보낸 코드와 서버가 저장 중인 코드 비교
        if (!(request.getCode()).equals(storedCode)) {
            throw new BusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 3. 인증 성공 처리
        // 3.1. 기존 인증 번호 삭제
        redisTemplate.delete(SMS_AUTH_PREFIX + request.getPhoneNumber());
        // 3.2. 인증 상태 키 저장
        redisTemplate.opsForValue()
                .set(SMS_VERIFIED_PREFIX + request.getPhoneNumber(), "VERIFIED", Duration.ofMinutes(30));
    }

    /**
     * 로컬 회원가입
     */
    @Transactional
    public void join(JoinRequest request) {
        // 1. 유저 ID 검증 (중복 확인)
        validateUsername(request.getUsername());

        // 2. PW 검증 (비밀번호/비밀번호 동등성 비교)
        validatePassword(request);

        // 3. 인증 여부 확인
        String verifiedStatus = redisTemplate.opsForValue().get(SMS_VERIFIED_PREFIX + request.getPhoneNumber());
        if (verifiedStatus == null) {
            // 인증을 안했거나 인증 유효 시간(30분)이 만료된 경우
            throw new BusinessException(AuthErrorCode.UNVERIFIED_PHONE_NUMBER);
        }

        // 4. 회원 저장 로직
        Member member = Member.ofLocal(
                request.getUsername(),
                bCryptPasswordEncoder.encode(request.getPassword()),
                generateRandomNickname(),
                request.getPhoneNumber()
        );
        memberRepository.save(member);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        // Spring Security가 인식할 수 있는 Spring Security 전용 authentication 객체(UsernamePasswordAuthenticationToken) 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        // 여기서 AuthenticationManager는 CustomUserDetailsService 사용하여 인증을 수행
        // ㄴ 즉, authenticationManager.authenticate(authenticationToken); 코드는 CustomUserDetailsService의 loadUserByUsername 메서드를 내부적으로 호출
        Authentication authentication = authenticationManager.authenticate(authToken);
        AuthMember authMember = (AuthMember) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(authMember);
        String refreshToken = jwtTokenProvider.createRefreshToken(authMember);

        redisTemplate.opsForValue()
                .set("RT:" + authMember.getMemberId(), refreshToken, Duration.ofMillis(604800000));

        return AuthResult.from(accessToken, refreshToken, authMember);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long memberId) {
        redisTemplate.delete("RT:" + memberId);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public AuthResult reissue(String refreshToken) {
        // 1. Refresh Token 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        // 2. 토큰에서 인증 객체 추출
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        // 3. Principal을 AuthMember로 형변환하여 memberId 획득
        AuthMember authMember = (AuthMember) authentication.getPrincipal();
        Long memberId = authMember.getMemberId();

        // 4. DB에서 유저 조회 (findById 사용)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 5. Redis에 저장된 Refresh Token 조회
        String savedToken = redisTemplate.opsForValue().get("RT:"+member.getId());

        // 6. 요청된 토큰과 Redis 저장 토큰 일치 여부 확인
        if (!refreshToken.equals(savedToken)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT_VALUE); // 토큰 불일치 (탈취 가능성 등)
        }

        // 7. 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(authMember);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authMember);

        // 8. Redis 갱신
        redisTemplate.opsForValue().set(
                "RT:"+member.getId(),
                newRefreshToken,
                Duration.ofMillis(604800000) // 7일
        );

        return AuthResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .loginResponse(null)
                .build();
    }


    /**
     * 비즈니스 로직
     */
    private void validateUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validatePassword(JoinRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(MemberErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
    }

    private String generateVerifyCode() {
        StringBuilder stringBuilder = new StringBuilder(6);

        for (int i=0; i<6; i++) {
            int index = ThreadLocalRandom.current().nextInt(CODE_CHARS.length());
            stringBuilder.append(CODE_CHARS.charAt(index));
        }

        return stringBuilder.toString();
    }

    private String generateRandomNickname() {
        Long seq = redisTemplate.opsForValue().increment(NICKNAME_SEQ_KEY);
        return NICKNAME_PREFIX + String.format("%08d", seq);
    }

}
