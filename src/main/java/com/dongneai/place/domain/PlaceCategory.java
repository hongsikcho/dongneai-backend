package com.dongneai.place.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {
    KIDS_CAFE("키즈카페"),
    PEDIATRICS("소아과"),
    PLAYGROUND("놀이터"),
    NURSING_ROOM("수유실"),
    CULTURE_CENTER("문화센터"),
    RESTAURANT("유아 동반 식당");

    private final String label;
}
