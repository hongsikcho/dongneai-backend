package com.dongneai.place.application;

import com.dongneai.place.application.dto.CreatePlaceRequest;
import com.dongneai.place.application.dto.ExternalPlaceResponse;
import com.dongneai.place.application.dto.PlaceDetailResponse;
import com.dongneai.place.application.dto.PlaceSummaryResponse;
import com.dongneai.place.application.port.ExternalPlaceSearch;
import com.dongneai.place.domain.Place;
import com.dongneai.place.domain.PlaceErrorCode;
import com.dongneai.place.domain.PlaceRepository;
import com.dongneai.place.domain.PlaceSearchCondition;
import com.dongneai.place.domain.PlaceSource;
import com.dongneai.place.infrastructure.PlaceQueryRepository;
import com.dongneai.region.api.RegionApi;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.exception.CommonErrorCode;
import com.dongneai.shared.response.CursorPage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final int EXTERNAL_SEARCH_SIZE = 15;

    private final PlaceRepository placeRepository;
    private final PlaceQueryRepository placeQueryRepository;
    private final PlaceAssembler assembler;
    private final RegionApi regionApi;
    private final ExternalPlaceSearch externalPlaceSearch;

    /** PLACE-01, MOD-01 */
    @Transactional(readOnly = true)
    public CursorPage<PlaceSummaryResponse> search(PlaceSearchCondition condition) {
        return assembler.toPage(placeQueryRepository.search(condition), condition.size(), condition.sort());
    }

    /** PLACE-02. 숨김·병합된 장소는 404 (병합된 경우 details에 대상 장소 ID). */
    @Transactional(readOnly = true)
    public PlaceDetailResponse detail(Long placeId) {
        Place place = placeRepository.findById(placeId).orElseThrow(() -> new ApiException(PlaceErrorCode.PLACE_NOT_FOUND));
        if (!place.isVisible()) {
            Map<String, Object> details = place.getMergedIntoId() == null ? null : Map.of("mergedIntoId", place.getMergedIntoId());
            throw new ApiException(PlaceErrorCode.PLACE_NOT_FOUND, PlaceErrorCode.PLACE_NOT_FOUND.getMessage(), details);
        }
        return assembler.toDetail(place);
    }

    /**
     * PLACE-04: 카카오 장소 검색. 지역 중심 좌표에서 반경 안만 찾는다 (좌표가 없으면 '지역 이름 + 검색어').
     * 이미 등록된 장소는 registeredPlaceId로 알려준다.
     */
    @Transactional(readOnly = true)
    public List<ExternalPlaceResponse> externalSearch(String query, Long regionId) {
        String q = query.strip();
        ExternalPlaceSearch.Near near = null;
        if (regionId != null) {
            RegionApi.SearchArea area = regionApi.findSearchArea(regionId)
                    .orElseThrow(() -> new ApiException(CommonErrorCode.INVALID_REQUEST, "서비스 지역이 아닙니다."));
            if (area.lat() != null && area.lng() != null) {
                near = new ExternalPlaceSearch.Near(area.lat(), area.lng(), area.radiusM());
            } else {
                q = area.fullName() + " " + q;
            }
        }
        List<ExternalPlaceSearch.ExternalPlace> docs = externalPlaceSearch.search(q, EXTERNAL_SEARCH_SIZE, near);
        Map<String, Long> registered = placeRepository.findByKakaoPlaceIdIn(docs.stream().map(ExternalPlaceSearch.ExternalPlace::id).toList())
                .stream().collect(Collectors.toMap(Place::getKakaoPlaceId, Place::getId));
        return docs.stream()
                .map(d -> new ExternalPlaceResponse(d.id(), d.name(), d.categoryName(), d.address(),
                        d.roadAddress(), decimal(d.lat()), decimal(d.lng()), registered.get(d.id())))
                .toList();
    }

    /**
     * PLACE-04: 사용자 장소 등록. 같은 카카오 장소가 있으면 409 + details.placeId.
     * 좌표가 있으면 실제 행정동을 확인해 그 좌표가 속한 서비스 지역으로 저장한다 (요청의 regionId보다 우선).
     */
    @Transactional
    public PlaceDetailResponse create(Long userId, CreatePlaceRequest request) {
        Long regionId = resolveRegion(request);
        rejectDuplicate(request.kakaoPlaceId());
        Place place = placeRepository.save(Place.builder()
                .regionId(regionId)
                .name(request.name().strip())
                .category(request.category())
                .address(request.address().strip())
                .lat(request.lat())
                .lng(request.lng())
                .kakaoPlaceId(blankToNull(request.kakaoPlaceId()))
                .source(PlaceSource.USER)
                .createdBy(userId)
                .build());
        return assembler.toDetail(place);
    }

    private Long resolveRegion(CreatePlaceRequest request) {
        if (request.lat() == null || request.lng() == null || !externalPlaceSearch.available()) {
            regionApi.requireOpenRegion(request.regionId());
            return request.regionId();
        }
        return externalPlaceSearch.areaCodeAt(request.lat(), request.lng())
                .flatMap(regionApi::findOpenRegionByAreaCode)
                .map(RegionApi.RegionSummary::id)
                .orElseThrow(() -> new ApiException(PlaceErrorCode.OUTSIDE_SERVICE_AREA));
    }

    void rejectDuplicate(String kakaoPlaceId) {
        if (kakaoPlaceId == null || kakaoPlaceId.isBlank()) {
            return;
        }
        placeRepository.findByKakaoPlaceId(kakaoPlaceId).ifPresent(existing -> {
            throw new ApiException(PlaceErrorCode.PLACE_ALREADY_EXISTS, PlaceErrorCode.PLACE_ALREADY_EXISTS.getMessage(),
                    Map.of("placeId", existing.getId()));
        });
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static BigDecimal decimal(String value) {
        try {
            return value == null ? null : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
