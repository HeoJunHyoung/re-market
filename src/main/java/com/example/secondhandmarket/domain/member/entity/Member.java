package com.example.secondhandmarket.domain.member.entity;

import com.example.secondhandmarket.domain.member.entity.enumerate.Role;
import com.example.secondhandmarket.domain.review.entity.enumerate.SafetyLevel;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "safety_score")
    @Min(1) @Max(1000)
    private Integer safetyScore; // 안심지수 (방패 모양)

    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    private Address address;

    // 일일 변화 제한을 위한 필드
    private LocalDate lastScoreUpdateDate;
    private Integer dailyIncrease = 0; // 오늘 오른 점수
    private Integer dailyDecrease = 0; // 오늘 떨어진 점수
    // 거래 횟수 (신규 계정 판단용)
    private Integer tradeCount = 0;

    @Version
    private Long version;

    // == 생성자 == //
    protected Member() { }

    private Member(String username, String password, String nickname, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.safetyScore = 300;
        this.role = Role.USER;
    }

    public static Member ofLocal(String username, String password, String nickname, String phoneNumber) {
        return new Member(username, password, nickname, phoneNumber);
    }

    // == 연관관계 편의 메서드 == //


    // == 비즈니스 로직 == //
    public void updateAddress(Address address) {
        this.address = address;
    }

    // 점수 업데이트 로직
    public void updateSafetyScore(int delta) {
        checkAndResetDailyLimit(); // 날짜가 바뀌었으면 초기화

        // 일일 제한 적용
        int actualDelta = applyDailyLimit(delta);

        // 최종 반영 (0 ~ 1000 Clamp)
        this.safetyScore = Math.max(0, Math.min(1000, this.safetyScore + actualDelta));
    }

    private void checkAndResetDailyLimit() {
        if (lastScoreUpdateDate == null || !lastScoreUpdateDate.equals(LocalDate.now())) {
            this.lastScoreUpdateDate = LocalDate.now();
            this.dailyIncrease = 0;
            this.dailyDecrease = 0;
        }
    }

    private int applyDailyLimit(int delta) {
        if (delta > 0) {
            // 상승 제한: 최대 +25
            int availableRise = 25 - this.dailyIncrease;
            int appliedRise = Math.min(delta, availableRise);
            this.dailyIncrease += appliedRise;
            return appliedRise;
        } else {
            // 하락 제한: 최대 -80 (delta는 음수)
            int availableDrop = -80 - this.dailyDecrease; // 예: 0 - (-80)은 안됨.
            // dailyDecrease는 음수로 누적
            int limit = -80;
            if (this.dailyDecrease <= limit) return 0; // 한계 도달

            int appliedDrop = Math.max(delta, limit - this.dailyDecrease);
            this.dailyDecrease += appliedDrop;
            return appliedDrop;
        }
    }

    public void incrementTradeCount() {
        this.tradeCount++;
    }

    // UX용 레벨 반환
    public SafetyLevel getSafetyLevel() {
        return SafetyLevel.of(this.safetyScore);
    }
}
