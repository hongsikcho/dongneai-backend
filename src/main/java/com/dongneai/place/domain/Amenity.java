package com.dongneai.place.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 후기에서 체크하는 이용 조건 */
@Getter
@RequiredArgsConstructor
public enum Amenity {
    PARKING("주차"),
    STROLLER("유모차 진입"),
    NURSING_ROOM("수유실"),
    DIAPER_TABLE("기저귀 교환대");

    private final String label;
}
