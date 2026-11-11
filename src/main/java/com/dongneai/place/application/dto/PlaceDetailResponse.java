package com.dongneai.place.application.dto;

import com.dongneai.place.domain.PlaceCategory;
import com.dongneai.place.domain.PlaceSource;
import com.dongneai.place.domain.PlaceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PlaceDetailResponse(
        Long id,
        String name,
        PlaceCategory category,
        RegionRef region,
        String address,
        BigDecimal lat,
        BigDecimal lng,
        String kakaoPlaceId,
        PlaceSource source,
        PlaceStatus status,
        LocalDate verifiedAt,
        int reviewCount,
        Double ratingAvg,
        int postCount,
        List<AmenityStatusResponse> amenities
) {
    public record RegionRef(Long id, String name) {
    }
}
