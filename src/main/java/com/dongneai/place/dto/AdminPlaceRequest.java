package com.dongneai.place.dto;

import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.AmenityStatus;
import com.dongneai.place.domain.PlaceCategory;
import com.dongneai.place.domain.PlaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

/**
 * ADM-05 장소 시딩·수정.
 *
 * @param amenities 운영자가 현장에서 확인한 편의시설 (없으면 변경 안 함)
 * @param verified  true면 오늘 날짜로 정보 확인일을 갱신
 * @param status    수정 시에만 사용 (숨김 처리 등)
 */
public record AdminPlaceRequest(
        @NotNull Long regionId,
        @NotBlank @Size(max = 100) String name,
        @NotNull PlaceCategory category,
        @NotBlank @Size(max = 200) String address,
        BigDecimal lat,
        BigDecimal lng,
        @Size(max = 30) String kakaoPlaceId,
        Map<Amenity, AmenityStatus> amenities,
        boolean verified,
        PlaceStatus status
) {
}
