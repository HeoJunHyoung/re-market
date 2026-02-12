package com.example.remarket.domain.item.entity.enumerate;

import lombok.Getter;

@Getter
public enum Category {
    DIGITAL_DEVICE("디지털기기"),
    FURNITURE_INTERIOR("가구/인테리어"),
    BABY_CHILD("유아동"),
    WOMEN_CLOTHING("여성의류"),
    WOMEN_ACCESSORIES("여성잡화"),
    MEN_FASHION_ACCESSORIES("남성패션/잡화"),
    HOME_APPLIANCE("생활가전"),
    LIFE_KITCHEN("생활/주방"),
    SPORTS_LEISURE("스포츠/레저"),
    HOBBY_GAME_MUSIC("취미/게임/음반"),
    BEAUTY_CARE("뷰티/미용"),
    PLANT("식물"),
    PROCESSED_FOOD("가공식품"),
    HEALTH_SUPPLEMENT("건강기능식품"),
    PET_SUPPLIES("반려동물용품"),
    TICKET_VOUCHER("티켓/교환권"),
    BOOK("도서"),
    OTHER("기타");

    private String description;

    Category(String description) {
        this.description = description;
    }
}
