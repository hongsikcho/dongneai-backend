package com.dongneai.place.application.dto;

import java.math.BigDecimal;

/**
 * 카카오 장소 검색 결과 1건.
 *
 * @param registeredPlaceId 이미 동네아이에 등록된 장소면 그 ID, 아니면 null
 */
public record ExternalPlaceResponse(
        String kakaoPlaceId,
        String name,
        String categoryName,
        String address,
        String roadAddress,
        BigDecimal lat,
        BigDecimal lng,
        Long registeredPlaceId
) {
}
