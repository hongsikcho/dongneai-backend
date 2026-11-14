package com.dongneai.region.application;

import com.dongneai.region.api.RegionApi;
import com.dongneai.region.domain.Region;
import com.dongneai.region.domain.RegionErrorCode;
import com.dongneai.region.domain.RegionRepository;
import com.dongneai.region.domain.UserRegionId;
import com.dongneai.region.domain.UserRegionRepository;
import com.dongneai.shared.exception.ApiException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegionApiService implements RegionApi {

    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;

    @Override
    @Transactional(readOnly = true)
    public void requireOpenRegion(Long regionId) {
        boolean ok = regionRepository.findById(regionId)
                .filter(Region::isActive)
                .isPresent();
        if (!ok) {
            throw new ApiException(RegionErrorCode.REGION_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void requireUserRegion(Long userId, Long regionId) {
        if (!userRegionRepository.existsById(new UserRegionId(userId, regionId))) {
            throw new ApiException(RegionErrorCode.NOT_MY_REGION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchArea> findSearchArea(Long regionId) {
        return regionRepository.findById(regionId)
                .filter(Region::isActive)
                .map(r -> new SearchArea(r.getId(), r.getName(), r.getFullName(), r.getLat(), r.getLng(),
                        r.getSearchRadiusM()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegionSummary> findOpenRegionByAreaCode(String areaCode) {
        return Region.mostSpecificOpenRegion(regionRepository.findByActiveTrue(), areaCode)
                .map(r -> new RegionSummary(r.getId(), r.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegionSummary> findRegion(Long regionId) {
        return regionRepository.findById(regionId).map(r -> new RegionSummary(r.getId(), r.getName()));
    }
}
