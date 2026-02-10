package com.example.remarket.global.security.service;

import com.example.remarket.domain.member.entity.Member;
import com.example.remarket.domain.member.exception.MemberErrorCode;
import com.example.remarket.domain.member.repository.MemberRepository;
import com.example.remarket.global.error.BusinessException;
import com.example.remarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        return new AuthMember(
                member.getId(),
                member.getUsername(),
                member.getPassword(),
                member.getRole()
        );
    }

}
