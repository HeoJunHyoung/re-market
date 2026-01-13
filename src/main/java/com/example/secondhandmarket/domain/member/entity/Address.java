package com.example.secondhandmarket.domain.member.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Address {

    private String city; // 시/도 (예: 서울특별시)

    private String district; // 구/군 (예: 강남구)

    private String neighborhood; // 동/읍/면 (예: 역삼동)

    public Address(String city, String district, String neighborhood) {
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
    }
}
