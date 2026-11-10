package com.dongneai.place.dto;

import com.dongneai.place.domain.PlaceCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** PLACE-04 사용자 장소 등록. 카카오 장소 검색 결과를 그대로 보낸다. */
public record CreatePlaceRequest(
        @NotNull Long regionId,
        @NotBlank @Size(max = 100) String name,
        @NotNull PlaceCategory category,
        @NotBlank @Size(max = 200) String address,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal lat,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal lng,
        @Size(max = 30) String kakaoPlaceId
) {
}
