package com.dongneai.place.domain;

import java.util.Set;

/**
 * @param includeHidden 관리자 목록에서만 true (숨김·병합 장소 포함)
 */
public record PlaceSearchCondition(
        Long regionId,
        PlaceCategory category,
        Set<Amenity> amenities,
        String keyword,
        PlaceSort sort,
        PlaceCursor cursor,
        int size,
        boolean includeHidden
) {
}
