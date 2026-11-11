package com.dongneai.place.application;

import com.dongneai.place.application.dto.AdminPlaceRequest;
import com.dongneai.place.application.dto.PlaceDetailResponse;
import com.dongneai.place.application.dto.PlaceSummaryResponse;
import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.AmenityStatus;
import com.dongneai.place.domain.Place;
import com.dongneai.place.domain.PlaceAmenity;
import com.dongneai.place.domain.PlaceAmenityId;
import com.dongneai.place.domain.PlaceAmenityRepository;
import com.dongneai.place.domain.PlaceErrorCode;
import com.dongneai.place.domain.PlaceRepository;
import com.dongneai.place.domain.PlaceSearchCondition;
import com.dongneai.place.domain.PlaceSource;
import com.dongneai.place.infrastructure.PlaceQueryRepository;
import com.dongneai.region.api.RegionApi;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.response.CursorPage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ADM-05 장소 관리 (시딩·수정·숨김) */
@Service
@RequiredArgsConstructor
public class AdminPlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceQueryRepository placeQueryRepository;
    private final PlaceAmenityRepository amenityRepository;
    private final PlaceAssembler assembler;
    private final RegionApi regionApi;
    private final PlaceService placeService;

    @Transactional(readOnly = true)
    public CursorPage<PlaceSummaryResponse> list(PlaceSearchCondition condition) {
        return placeService.search(condition);
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse detail(Long placeId) {
        return assembler.toDetail(get(placeId));
    }

    @Transactional
    public PlaceDetailResponse create(Long adminId, AdminPlaceRequest request) {
        regionApi.requireOpenDong(request.regionId());
        placeService.rejectDuplicate(request.kakaoPlaceId());
        Place place = placeRepository.save(Place.builder()
                .regionId(request.regionId())
                .name(request.name().strip())
                .category(request.category())
                .address(request.address().strip())
                .lat(request.lat())
                .lng(request.lng())
                .kakaoPlaceId(PlaceService.blankToNull(request.kakaoPlaceId()))
                .source(PlaceSource.ADMIN)
                .createdBy(adminId)
                .verifiedAt(request.verified() ? LocalDate.now() : null)
                .build());
        applyAmenities(place.getId(), request.amenities());
        return assembler.toDetail(place);
    }

    @Transactional
    public PlaceDetailResponse update(Long placeId, AdminPlaceRequest request) {
        Place place = get(placeId);
        regionApi.requireOpenDong(request.regionId());
        String kakaoId = PlaceService.blankToNull(request.kakaoPlaceId());
        if (kakaoId != null && !Objects.equals(kakaoId, place.getKakaoPlaceId())) {
            placeService.rejectDuplicate(kakaoId);
        }
        place.update(request.regionId(), request.name().strip(), request.category(), request.address().strip(),
                request.lat(), request.lng());
        if (request.status() != null) {
            place.changeStatus(request.status());
        }
        if (request.verified()) {
            place.markVerified(LocalDate.now());
        }
        applyAmenities(placeId, request.amenities());
        return assembler.toDetail(place);
    }

    private void applyAmenities(Long placeId, Map<Amenity, AmenityStatus> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        amenities.forEach((amenity, status) -> amenityRepository.findById(new PlaceAmenityId(placeId, amenity))
                .ifPresentOrElse(
                        row -> row.setByAdmin(status, now),
                        () -> amenityRepository.save(new PlaceAmenity(placeId, amenity, status, 0, now))));
    }

    private Place get(Long placeId) {
        return placeRepository.findById(placeId).orElseThrow(() -> new ApiException(PlaceErrorCode.PLACE_NOT_FOUND));
    }
}
