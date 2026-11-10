package com.dongneai.place.application;

import com.dongneai.common.exception.ApiException;
import com.dongneai.common.exception.ErrorCode;
import com.dongneai.region.domain.Region;
import com.dongneai.region.domain.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 장소는 서비스 오픈된 읍면동에만 등록할 수 있다. */
@Component
@RequiredArgsConstructor
class RegionGuard {

    private final RegionRepository regionRepository;

    void requireOpenDong(Long regionId) {
        boolean ok = regionRepository.findById(regionId)
                .filter(r -> r.isActive() && r.getLevel() == Region.LEVEL_DONG)
                .isPresent();
        if (!ok) {
            throw new ApiException(ErrorCode.REGION_NOT_FOUND);
        }
    }
}
