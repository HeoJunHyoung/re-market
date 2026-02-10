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

    @PatchMapping("/location")
    public ResponseEntity<Void> updateLocation(@AuthenticationPrincipal AuthMember authMember,
                                               @RequestBody LocationUpdateRequest request) {

        Address address = new Address(request.getCity(), request.getDistrict(), request.getNeighborhood());
        memberService.updateMemberLocation(authMember.getMemberId(), address);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-location")
    public ResponseEntity<MessageResponse> verifyLocation(@AuthenticationPrincipal AuthMember authMember,
                                                          @RequestBody LocationVerifyRequest request) {
        boolean isVerified = memberService.verifyMemberLocation(authMember.getMemberId(), request);

        if (isVerified) {
            return ResponseEntity.ok(new MessageResponse("동네 인증에 성공했습니다!"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("현재 위치가 설정된 동네와 다릅니다."));
        }
    }

}
