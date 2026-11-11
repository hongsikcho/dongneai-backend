package com.dongneai.region.application;

import com.dongneai.region.api.RegionApi;
import com.dongneai.region.domain.Region;
import com.dongneai.region.domain.RegionErrorCode;
import com.dongneai.region.domain.RegionRepository;
import com.dongneai.shared.exception.ApiException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegionApiService implements RegionApi {

    private final RegionRepository regionRepository;

    @Override
    @Transactional(readOnly = true)
    public void requireOpenDong(Long regionId) {
        boolean ok = regionRepository.findById(regionId)
                .filter(r -> r.isActive() && r.getLevel() == Region.LEVEL_DONG)
                .isPresent();
        if (!ok) {
            throw new ApiException(RegionErrorCode.REGION_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegionSummary> findRegion(Long regionId) {
        return regionRepository.findById(regionId).map(r -> new RegionSummary(r.getId(), r.getName()));
    }
}
