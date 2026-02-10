package com.example.remarket.domain.member.service;

import com.example.remarket.domain.member.dto.response.MemberResponse;
import com.example.remarket.domain.member.entity.Member;
import com.example.remarket.domain.member.repository.MemberRepository;
import com.example.remarket.global.error.BusinessException;
import com.example.remarket.domain.member.entity.Address;
import com.example.remarket.domain.member.exception.MemberErrorCode;
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
