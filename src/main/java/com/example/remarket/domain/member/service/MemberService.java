package com.example.remarket.domain.member.service;

import com.example.remarket.domain.member.dto.request.LocationVerifyRequest;
import com.example.remarket.domain.member.dto.response.MemberResponse;
import com.example.remarket.domain.member.entity.Member;
import com.example.remarket.domain.member.repository.MemberRepository;
import com.example.remarket.domain.region.exception.RegionErrorCode;
import com.example.remarket.global.error.BusinessException;
import com.example.remarket.domain.member.entity.Address;
import com.example.remarket.domain.member.exception.MemberErrorCode;
import com.example.remarket.global.error.GlobalErrorCode;
import com.example.remarket.global.util.GeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final GeoService geoService;

    public MemberResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.fromEntity(member);
    }

    @Transactional
    public void verifyMemberLocation(Long memberId, LocationVerifyRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 1. 좌표(위도, 경도)를 통해 실제 행정 구역 주소 가져오기
        Address currentAddress = geoService.getAddressFromCoordinates(request.getLatitude(), request.getLongitude());

        if (currentAddress == null) {
            throw new BusinessException(RegionErrorCode.REGION_NOT_FOUND); // 적절한 예외 처리
        }

        // 2. 주소 업데이트 및 인증 완료 처리 (한 번에 수행)
        member.updateAddress(currentAddress); // 주소 갱신
        member.verifyLocation();              // 인증 상태 true 변경

        log.info("사용자 {} 위치 인증 및 갱신 완료: {}", memberId, currentAddress.getNeighborhood());
    }

}
