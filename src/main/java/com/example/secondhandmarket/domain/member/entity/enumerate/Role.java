package com.example.secondhandmarket.domain.member.entity.enumerate;

import lombok.Getter;

@Getter
public enum Role {
    USER("사용자"),
    ADMIN("관리자");

    private String description;

    Role(String description) {
        this.description = description;
    }

}
