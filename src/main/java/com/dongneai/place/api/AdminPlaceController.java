package com.dongneai.place.api;

import com.dongneai.common.response.CursorPage;
import com.dongneai.place.application.AdminPlaceService;
import com.dongneai.place.domain.PlaceCategory;
import com.dongneai.place.domain.PlaceCursor;
import com.dongneai.place.domain.PlaceSearchCondition;
import com.dongneai.place.domain.PlaceSort;
import com.dongneai.place.dto.AdminPlaceRequest;
import com.dongneai.place.dto.PlaceDetailResponse;
import com.dongneai.place.dto.PlaceSummaryResponse;
import com.dongneai.security.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** ADM-05 장소 관리. /api/v1/admin/** 는 SecurityConfig에서 ADMIN 권한만 허용. */
@RestController
@RequestMapping("/api/v1/admin/places")
@RequiredArgsConstructor
public class AdminPlaceController {

    private final AdminPlaceService adminPlaceService;

    /** 숨김·병합 장소 포함 */
    @GetMapping
    public CursorPage<PlaceSummaryResponse> list(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) PlaceCategory category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return adminPlaceService.list(new PlaceSearchCondition(regionId, category, Set.of(), q, PlaceSort.LATEST,
                PlaceCursor.parse(cursor, PlaceSort.LATEST), size, true));
    }

    @GetMapping("/{placeId}")
    public PlaceDetailResponse detail(@PathVariable Long placeId) {
        return adminPlaceService.detail(placeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceDetailResponse create(@AuthenticationPrincipal AuthUser admin,
                                      @RequestBody @Valid AdminPlaceRequest request) {
        return adminPlaceService.create(admin.userId(), request);
    }

    @PatchMapping("/{placeId}")
    public PlaceDetailResponse update(@PathVariable Long placeId, @RequestBody @Valid AdminPlaceRequest request) {
        return adminPlaceService.update(placeId, request);
    }
}
