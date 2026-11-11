package com.dongneai.shared.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 장소 이용 조건 (주차·유모차·수유실·기저귀대). 장소 표시와 후기 체크에서 함께 쓴다. */
@Getter
@RequiredArgsConstructor
public enum Amenity {
    PARKING("주차"),
    STROLLER("유모차 진입"),
    NURSING_ROOM("수유실"),
    DIAPER_TABLE("기저귀 교환대");

    private final String label;
}
