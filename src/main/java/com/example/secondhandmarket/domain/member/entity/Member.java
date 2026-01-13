package com.example.secondhandmarket.domain.member.entity;

import com.example.secondhandmarket.domain.member.entity.enumerate.Role;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id; // 자체 고유 ID

    @Column(name = "username", unique = true)
    private String username; // 유저 ID (로그인)

    @Column(name = "password")
    private String password; // 유저 PW (로그인)

    @Column(name = "nickname", unique = true)
    private String nickname; // 유저 닉네임

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "safety_score")
    @Min(1) @Max(1000)
    private Integer safetyScore; // 안심지수 (방패 모양)

    @Enumerated(EnumType.STRING)
    private Role role;

    // == 생성자 == //
    protected Member() { }

    private Member(String username, String password, String nickname, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.safetyScore = 500;
        this.role = Role.USER;
    }

    public static Member ofLocal(String username, String password, String nickname, String phoneNumber) {
        return new Member(username, password, nickname, phoneNumber);
    }

    // == 연관관계 편의 메서드 == //


    // == 비즈니스 로직 == //

}
