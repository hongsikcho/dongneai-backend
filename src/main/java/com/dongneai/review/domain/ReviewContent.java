package com.dongneai.review.domain;

import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.exception.CommonErrorCode;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;

/** 후기 작성·수정 값. 생성 시점에 도메인 규칙을 검사한다. */
public record ReviewContent(
        YearMonth visitedOn,
        AgeGroup childAgeGroup,
        int rating,
        String content,
        Map<Amenity, AmenityStatus> amenityChecks
) {
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 1000;

    public ReviewContent {
        if (visitedOn == null || childAgeGroup == null) {
            throw new ApiException(CommonErrorCode.INVALID_REQUEST);
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new ApiException(CommonErrorCode.INVALID_REQUEST, "별점은 1~5점으로 선택해 주세요.");
        }
        content = content == null ? "" : content.strip();
        if (content.length() < MIN_LENGTH || content.length() > MAX_LENGTH) {
            throw new ApiException(ReviewErrorCode.INVALID_REVIEW_CONTENT);
        }
        Map<Amenity, AmenityStatus> checks = new EnumMap<>(Amenity.class);
        if (amenityChecks != null) {
            amenityChecks.forEach((amenity, status) -> {
                if (amenity != null && status != null) {
                    checks.put(amenity, status);
                }
            });
        }
        amenityChecks = Map.copyOf(checks);
    }

    /** 방문 월은 서비스 기준(한국 시간) 이번 달까지만 허용한다. */
    public void requireNotFuture(YearMonth currentMonth) {
        if (visitedOn.isAfter(currentMonth)) {
            throw new ApiException(ReviewErrorCode.INVALID_VISIT_MONTH);
        }
    }
}
