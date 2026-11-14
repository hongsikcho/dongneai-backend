package com.dongneai.place.presentation;

import com.dongneai.place.application.PlaceService;
import com.dongneai.place.application.dto.CreatePlaceRequest;
import com.dongneai.place.application.dto.ExternalPlaceResponse;
import com.dongneai.place.application.dto.PlaceDetailResponse;
import com.dongneai.place.application.dto.PlaceSummaryResponse;
import com.dongneai.place.domain.PlaceCategory;
import com.dongneai.place.domain.PlaceCursor;
import com.dongneai.place.domain.PlaceSearchCondition;
import com.dongneai.place.domain.PlaceSort;
import com.dongneai.security.AuthUser;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.response.CursorPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    /**
     * PLACE-01 장소 목록.
     * amenities는 콤마 구분, 모두 YES인 장소만 (AND). ageGroup 필터는 후기 기능 이후 추가.
     */
    @GetMapping
    public CursorPage<PlaceSummaryResponse> search(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) PlaceCategory category,
            @RequestParam(required = false) List<Amenity> amenities,
            @RequestParam(required = false) @Size(max = 50) String q,
            @RequestParam(defaultValue = "LATEST") PlaceSort sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        Set<Amenity> amenitySet = amenities == null || amenities.isEmpty() ? Set.of() : EnumSet.copyOf(amenities);
        return placeService.search(new PlaceSearchCondition(regionId, category, amenitySet, q, sort,
                PlaceCursor.parse(cursor, sort), size, false));
    }

    /** PLACE-02 */
    @GetMapping("/{placeId:\\d+}")
    public PlaceDetailResponse detail(@PathVariable Long placeId) {
        return placeService.detail(placeId);
    }

    /**
     * PLACE-04 카카오 장소 검색. regionId가 있으면 그 지역 중심 반경 안에서만,
     * category가 있으면 그 업종 위주로 찾는다. 부적합 업종은 빠지고 아이와 가기 좋은 곳이 위로 온다.
     */
    @GetMapping("/external-search")
    public List<ExternalPlaceResponse> externalSearch(@RequestParam @NotBlank @Size(max = 50) String q,
                                                      @RequestParam(required = false) Long regionId,
                                                      @RequestParam PlaceCategory category) {
        return placeService.externalSearch(q, regionId, category);
    }

    /** PLACE-04 장소 등록 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceDetailResponse create(@AuthenticationPrincipal AuthUser user,
                                      @RequestBody @Valid CreatePlaceRequest request) {
        return placeService.create(user.userId(), request);
    }
}
