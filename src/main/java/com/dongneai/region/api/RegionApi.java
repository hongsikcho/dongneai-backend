package com.dongneai.region.api;

import java.util.Optional;

/** 다른 모듈이 지역을 확인할 때 쓰는 공개 API. */
public interface RegionApi {

    /** 서비스가 열린 읍면동인지 확인한다. 아니면 REGION_NOT_FOUND. */
    void requireOpenDong(Long regionId);

    /** 지역 이름 조회 (응답 표시용). 없으면 빈 값. */
    Optional<RegionSummary> findRegion(Long regionId);

    record RegionSummary(Long id, String name) {
    }
}
