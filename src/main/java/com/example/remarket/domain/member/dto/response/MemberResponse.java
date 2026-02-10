package com.example.remarket.domain.member.dto.response;

import com.example.remarket.domain.member.entity.Member;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {

    private Long memberId;
    private String username;
    private String nickname;
    private String role;

    public static MemberResponse fromEntity(Member member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .role(String.valueOf(member.getRole()))
                .build();
    }
}
