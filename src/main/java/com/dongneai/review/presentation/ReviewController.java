package com.dongneai.review.presentation;

import com.dongneai.review.application.ReviewService;
import com.dongneai.review.application.dto.ReviewRequest;
import com.dongneai.review.application.dto.ReviewResponse;
import com.dongneai.security.AuthUser;
import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.response.CursorPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** PLACE-02 후기 탭. 최신순, ageGroup으로 아이 연령대 필터. */
    @GetMapping("/places/{placeId:\\d+}/reviews")
    public CursorPage<ReviewResponse> list(@AuthenticationPrincipal AuthUser user,
                                           @PathVariable Long placeId,
                                           @RequestParam(required = false) AgeGroup ageGroup,
                                           @RequestParam(required = false) String cursor,
                                           @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return reviewService.list(user.userId(), placeId, ageGroup, cursor, size);
    }

    /** PLACE-03 후기 작성. 같은 달 후기가 있으면 409 + details.reviewId */
    @PostMapping("/places/{placeId:\\d+}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal AuthUser user,
                                 @PathVariable Long placeId,
                                 @RequestBody @Valid ReviewRequest request) {
        return reviewService.create(user.userId(), placeId, request);
    }

    /** PLACE-03 수정 화면 진입 */
    @GetMapping("/reviews/{reviewId:\\d+}")
    public ReviewResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long reviewId) {
        return reviewService.get(user.userId(), reviewId);
    }

    /** PLACE-03 후기 수정 (작성자만) */
    @PatchMapping("/reviews/{reviewId:\\d+}")
    public ReviewResponse update(@AuthenticationPrincipal AuthUser user,
                                 @PathVariable Long reviewId,
                                 @RequestBody @Valid ReviewRequest request) {
        return reviewService.update(user.userId(), reviewId, request);
    }

    /** 후기 삭제 (작성자만) */
    @DeleteMapping("/reviews/{reviewId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long reviewId) {
        reviewService.delete(user.userId(), reviewId);
    }
}
