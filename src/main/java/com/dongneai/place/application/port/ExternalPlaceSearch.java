package com.dongneai.place.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** 외부 장소 검색 포트. 구현은 infrastructure의 카카오 로컬 API 클라이언트. */
public interface ExternalPlaceSearch {

    /** 키워드 검색. near가 있으면 그 좌표 반경 안에서만 찾는다. */
    List<ExternalPlace> search(String query, int size, Near near);

    /** 좌표가 속한 행정동 코드 (10자리, 예: "4117352000"). 찾지 못하면 빈 값. */
    Optional<String> areaCodeAt(BigDecimal lat, BigDecimal lng);

    /** 키 설정 여부. 로컬에서 키 없이 띄웠을 때 좌표 검증을 건너뛰는 데 쓴다. */
    boolean available();

    /** 검색 기준점과 반경(m, 최대 20000). */
    record Near(BigDecimal lat, BigDecimal lng, int radiusM) {
    }

    /** lng = 경도(x), lat = 위도(y). 외부 응답 그대로의 문자열. */
    record ExternalPlace(String id, String name, String categoryName, String address, String roadAddress,
                         String lng, String lat) {
    }
}
