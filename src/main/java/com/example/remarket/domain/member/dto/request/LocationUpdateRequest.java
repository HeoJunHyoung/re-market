package com.example.remarket.domain.member.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationUpdateRequest {
    private String city;
    private String district;
    private String neighborhood;
}