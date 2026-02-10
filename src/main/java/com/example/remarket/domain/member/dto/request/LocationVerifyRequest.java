package com.example.remarket.domain.member.dto.request;

import lombok.Data;

@Data
public class LocationVerifyRequest {
    private Double latitude;  // 위도 (y)
    private Double longitude; // 경도 (x)
}