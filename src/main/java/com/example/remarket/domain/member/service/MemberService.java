package com.example.remarket.domain.member.service;

import com.example.remarket.domain.member.dto.request.LocationVerifyRequest;
import com.example.remarket.domain.member.dto.response.MemberResponse;
import com.example.remarket.domain.member.entity.Member;
import com.example.remarket.domain.member.repository.MemberRepository;
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
    public void updateMemberLocation(Long memberId, Address address) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.updateAddress(address);
    }

    @Transactional
    public boolean verifyMemberLocation(Long memberId, LocationVerifyRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (member.getAddress() == null) {
            throw new IllegalArgumentException("먼저 동네를 설정해주세요.");
        }

        // 1. 현재 좌표의 동네 이름 조회 (단순 문자열 비교용)
        String realNeighborhood = geoService.getRegionName(request.getLatitude(), request.getLongitude());
        String userNeighborhood = member.getAddress().getNeighborhood();

        log.info("사용자 설정 동네: {}, 실제 감지된 동네: {}", userNeighborhood, realNeighborhood);

        // 2.1. 이름이 일치하거나 포함되면 즉시 통과
        if (realNeighborhood != null &&
                (realNeighborhood.contains(userNeighborhood) || userNeighborhood.contains(realNeighborhood))) {
            return true;
        }

        // 2.2. 이름이 다르더라도 거리 기반 오차 허용 (Widen Margin)
        String fullAddress = member.getAddress().getCity() + " " +
                member.getAddress().getDistrict() + " " +
                userNeighborhood;

        Map<String, Double> setCoords = geoService.getCoordinates(fullAddress);

        if (setCoords != null) {
            double distance = geoService.calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    setCoords.get("lat"), setCoords.get("lng")
            );

            log.info("동네 중심점과의 거리: {} km", String.format("%.2f", distance));

            // 반경 3km 이내라면 인증 승인
            if (distance <= 3.0) {
                log.info("반경 3km 이내 확인됨. 인증 승인");
                member.verifyLocation(); // DB에 인증됨(true)으로 기록
                return true;
            }
        }

        return false;
    }

}
