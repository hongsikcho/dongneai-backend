package com.dongneai.review.application.dto;

import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** PLACE-03 후기 작성·수정. amenities 예: {"PARKING":"YES","STROLLER":"NO"} (없는 항목은 체크 안 함) */
public record ReviewRequest(
        @NotNull @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "방문 월은 YYYY-MM 형식이어야 합니다.") String visitedOn,
        @NotNull AgeGroup childAgeGroup,
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 2000) String content,
        Map<Amenity, AmenityStatus> amenities
) {
}
