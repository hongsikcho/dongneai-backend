package com.dongneai.region.application;

import com.dongneai.common.exception.ApiException;
import com.dongneai.common.exception.ErrorCode;
import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserRepository;
import com.dongneai.region.domain.Region;
import com.dongneai.region.domain.RegionRepository;
import com.dongneai.region.domain.UserRegion;
import com.dongneai.region.domain.UserRegionRepository;
import com.dongneai.region.dto.UserRegionResponse;
import com.dongneai.region.dto.UserRegionsRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegionService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;

    /** MY-04: 기본 동네가 먼저 오도록 정렬 */
    @Transactional(readOnly = true)
    public List<UserRegionResponse> myRegions(Long userId) {
        List<UserRegion> links = userRegionRepository.findByUserId(userId);
        Map<Long, Region> regions = regionRepository.findAllById(links.stream().map(UserRegion::getRegionId).toList())
                .stream().collect(Collectors.toMap(Region::getId, Function.identity()));
        return links.stream()
                .sorted(Comparator.comparing(UserRegion::isPrimaryRegion).reversed()
                        .thenComparing(UserRegion::getCreatedAt))
                .map(link -> UserRegionResponse.of(regions.get(link.getRegionId()), link.isPrimaryRegion()))
                .toList();
    }

    /**
     * AUTH-05, MY-04: 관심 지역 전체 교체. 1~3개, 기본 1개, 서비스 오픈된 동만 허용.
     * 프로필 설정 전이면 403, REGION 단계면 DONE으로 진행.
     */
    @Transactional
    public List<UserRegionResponse> replace(Long userId, UserRegionsRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (!user.hasReached(OnboardingStep.REGION)) {
            throw new ApiException(ErrorCode.ONBOARDING_REQUIRED);
        }

        List<UserRegionsRequest.Item> items = request.regions();
        List<Long> ids = items.stream().map(UserRegionsRequest.Item::regionId).distinct().toList();
        long primaryCount = items.stream().filter(UserRegionsRequest.Item::primary).count();
        if (ids.isEmpty() || ids.size() != items.size() || ids.size() > UserRegion.MAX_PER_USER || primaryCount != 1) {
            throw new ApiException(ErrorCode.INVALID_REGION_SELECTION);
        }

        List<Region> regions = regionRepository.findAllById(ids);
        boolean allSelectable = regions.size() == ids.size()
                && regions.stream().allMatch(r -> r.isActive() && r.getLevel() == Region.LEVEL_DONG);
        if (!allSelectable) {
            throw new ApiException(ErrorCode.REGION_NOT_FOUND);
        }

        userRegionRepository.deleteAllByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        userRegionRepository.saveAll(items.stream()
                .map(item -> new UserRegion(userId, item.regionId(), item.primary(), now))
                .toList());

        // deleteAllByUserId가 영속성 컨텍스트를 비웠으므로 회원을 다시 읽어 단계를 갱신한다
        userRepository.findById(userId).ifPresent(u -> u.completeOnboardingStep(OnboardingStep.REGION));
        return myRegions(userId);
    }
}
