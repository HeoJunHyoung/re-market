package com.example.secondhandmarket.domain.member.service;

import com.example.secondhandmarket.domain.member.dto.response.MemberResponse;
import com.example.secondhandmarket.domain.member.entity.Address;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.exception.MemberErrorCode;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import com.example.secondhandmarket.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

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

}
