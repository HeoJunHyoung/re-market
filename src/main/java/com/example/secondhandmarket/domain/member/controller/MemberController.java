package com.example.secondhandmarket.domain.member.controller;

import com.example.secondhandmarket.domain.member.dto.response.MemberResponse;
import com.example.secondhandmarket.domain.member.service.MemberService;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(@AuthenticationPrincipal AuthMember authMember) {
        MemberResponse memberResponse = memberService.getMe(authMember.getMemberId());
        return ResponseEntity.ok(memberResponse);
    }

}
