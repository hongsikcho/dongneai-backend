package com.dongneai.place.application;

import com.dongneai.place.application.dto.AmenityStatusResponse;
import com.dongneai.place.application.dto.PlaceDetailResponse;
import com.dongneai.place.application.dto.PlaceSummaryResponse;
import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.AmenityStatus;
import com.dongneai.place.domain.Place;
import com.dongneai.place.domain.PlaceAmenity;
import com.dongneai.place.domain.PlaceAmenityRepository;
import com.dongneai.place.domain.PlaceCursor;
import com.dongneai.place.domain.PlaceSort;
import com.dongneai.region.api.RegionApi;
import com.dongneai.shared.response.CursorPage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 엔티티 → 응답 변환. 편의시설 조회를 묶어서 N+1을 피한다. */
@Component
@RequiredArgsConstructor
class PlaceAssembler {

    private final PlaceAmenityRepository amenityRepository;
    private final RegionApi regionApi;

    CursorPage<PlaceSummaryResponse> toPage(List<Place> fetched, int size, PlaceSort sort) {
        CursorPage<Place> page = CursorPage.of(fetched, size, p -> PlaceCursor.of(p, sort));
        List<Long> ids = page.items().stream().map(Place::getId).toList();
        Map<Long, List<Amenity>> confirmed = amenityRepository.findByPlaceIdIn(ids).stream()
                .filter(a -> a.getStatus() == AmenityStatus.YES)
                .collect(Collectors.groupingBy(PlaceAmenity::getPlaceId,
                        Collectors.mapping(PlaceAmenity::getAmenity, Collectors.toList())));
        List<PlaceSummaryResponse> items = page.items().stream()
                .map(p -> PlaceSummaryResponse.of(p, sorted(confirmed.getOrDefault(p.getId(), List.of()))))
                .toList();
        return new CursorPage<>(items, page.nextCursor());
    }

    PlaceDetailResponse toDetail(Place place) {
        Map<Amenity, PlaceAmenity> rows = amenityRepository.findByPlaceId(place.getId()).stream()
                .collect(Collectors.toMap(PlaceAmenity::getAmenity, Function.identity()));
        List<AmenityStatusResponse> amenities = Arrays.stream(Amenity.values())
                .map(a -> {
                    PlaceAmenity row = rows.get(a);
                    return row == null
                            ? new AmenityStatusResponse(a, AmenityStatus.UNKNOWN, 0)
                            : new AmenityStatusResponse(a, row.getStatus(), row.getConfirmedCount());
                })
                .toList();
        var regionRef = regionApi.findRegion(place.getRegionId())
                .map(r -> new PlaceDetailResponse.RegionRef(r.id(), r.name()))
                .orElse(null);
        return new PlaceDetailResponse(place.getId(), place.getName(), place.getCategory(), regionRef,
                place.getAddress(), place.getLat(), place.getLng(), place.getKakaoPlaceId(), place.getSource(),
                place.getStatus(), place.getVerifiedAt(), place.getReviewCount(), place.getPostCount(), amenities);
    }

    private static List<Amenity> sorted(List<Amenity> amenities) {
        return amenities.stream().sorted().toList();
    }
}
