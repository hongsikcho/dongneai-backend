package com.dongneai.place.application.port;

import java.util.List;

/** 외부 장소 검색 포트. 구현은 infrastructure의 카카오 로컬 API 클라이언트. */
public interface ExternalPlaceSearch {

    List<ExternalPlace> search(String query, int size);

    /** lng = 경도(x), lat = 위도(y). 외부 응답 그대로의 문자열. */
    record ExternalPlace(String id, String name, String categoryName, String address, String roadAddress,
                         String lng, String lat) {
    }
}
