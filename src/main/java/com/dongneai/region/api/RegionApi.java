package com.dongneai.region.api;

import java.math.BigDecimal;
import java.util.Optional;

/** 다른 모듈이 지역을 확인할 때 쓰는 공개 API. */
public interface RegionApi {

    /** 서비스가 열린 지역(시·구·동)인지 확인한다. 아니면 REGION_NOT_FOUND. */
    void requireOpenRegion(Long regionId);

    /** 장소 등록 검색의 기준점 (지역 중심 좌표와 반경). 좌표가 없으면 이름만 채워 준다. */
    Optional<SearchArea> findSearchArea(Long regionId);

    /** 행정구역 코드(카카오 좌표→행정동 코드 등)가 속한 서비스 지역 중 가장 좁은 곳. 서비스 밖이면 빈 값. */
    Optional<RegionSummary> findOpenRegionByAreaCode(String areaCode);

    /** lat·lng가 null이면 좌표 없이 이름으로만 검색한다. */
    record SearchArea(Long regionId, String name, String fullName, BigDecimal lat, BigDecimal lng, int radiusM) {
    }

    /** 회원이 관심 동네로 설정한 지역인지 확인한다. 아니면 NOT_MY_REGION (403). */
    void requireUserRegion(Long userId, Long regionId);

    /** 지역 이름 조회 (응답 표시용). 없으면 빈 값. */
    Optional<RegionSummary> findRegion(Long regionId);

    record RegionSummary(Long id, String name) {
    }
}
