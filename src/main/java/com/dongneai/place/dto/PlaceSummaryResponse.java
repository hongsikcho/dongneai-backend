package com.dongneai.place.dto;

import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.Place;
import com.dongneai.place.domain.PlaceCategory;
import java.time.LocalDate;
import java.util.List;

/** 장소 카드. amenities에는 YES로 확인된 편의시설만 담는다. */
public record PlaceSummaryResponse(
        Long id,
        String name,
        PlaceCategory category,
        String address,
        int reviewCount,
        List<Amenity> amenities,
        LocalDate verifiedAt
) {
    public static PlaceSummaryResponse of(Place place, List<Amenity> confirmedAmenities) {
        return new PlaceSummaryResponse(place.getId(), place.getName(), place.getCategory(), place.getAddress(),
                place.getReviewCount(), confirmedAmenities, place.getVerifiedAt());
    }
}
