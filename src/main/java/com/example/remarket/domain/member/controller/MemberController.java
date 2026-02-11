package com.example.remarket.domain.member.controller;

import com.example.remarket.domain.member.dto.request.LocationUpdateRequest;
import com.example.remarket.domain.member.dto.request.LocationVerifyRequest;
import com.example.remarket.domain.member.dto.response.MemberResponse;
import com.example.remarket.domain.member.dto.response.MessageResponse;
import com.example.remarket.domain.member.service.MemberService;
import com.example.remarket.domain.member.entity.Address;
import com.example.remarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/verify-location")
    public ResponseEntity<MessageResponse> verifyLocation(@AuthenticationPrincipal AuthMember authMember,
                                                          @RequestBody LocationVerifyRequest request) {
        memberService.verifyMemberLocation(authMember.getMemberId(), request);
        return ResponseEntity.ok(new MessageResponse("동네 인증에 성공했습니다!"));
    }

}
