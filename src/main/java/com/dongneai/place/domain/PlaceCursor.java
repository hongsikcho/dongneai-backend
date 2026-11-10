package com.dongneai.place.domain;

import com.dongneai.common.exception.ApiException;
import com.dongneai.common.exception.ErrorCode;

/**
 * 장소 목록 커서. LATEST는 "id", REVIEWS는 "reviewCount_id" 형태의 문자열로 주고받는다.
 */
public record PlaceCursor(int reviewCount, long id) {

    public static PlaceCursor parse(String value, PlaceSort sort) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            if (sort == PlaceSort.REVIEWS) {
                String[] parts = value.split("_", 2);
                return new PlaceCursor(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
            }
            return new PlaceCursor(0, Long.parseLong(value));
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "cursor 값이 올바르지 않습니다.");
        }
    }

    public static String of(Place place, PlaceSort sort) {
        return sort == PlaceSort.REVIEWS ? place.getReviewCount() + "_" + place.getId() : String.valueOf(place.getId());
    }
}
