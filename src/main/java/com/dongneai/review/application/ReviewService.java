package com.dongneai.review.application;

import com.dongneai.member.api.MemberApi;
import com.dongneai.place.api.PlaceApi;
import com.dongneai.review.application.dto.ReviewRequest;
import com.dongneai.review.application.dto.ReviewResponse;
import com.dongneai.review.domain.Review;
import com.dongneai.review.domain.ReviewContent;
import com.dongneai.review.domain.ReviewErrorCode;
import com.dongneai.review.domain.ReviewRepository;
import com.dongneai.review.infrastructure.ReviewQueryRepository;
import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.exception.CommonErrorCode;
import com.dongneai.shared.response.CursorPage;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PLACE-02 후기 목록, PLACE-03 후기 작성·수정·삭제. */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final PlaceApi placeApi;
    private final MemberApi memberApi;

    @Transactional(readOnly = true)
    public CursorPage<ReviewResponse> list(Long viewerId, Long placeId, AgeGroup ageGroup, String cursor, int size) {
        placeApi.requireVisible(placeId);
        List<Review> fetched = reviewQueryRepository.findVisibleByPlace(placeId, ageGroup, parseCursor(cursor), size);
        CursorPage<Review> page = CursorPage.of(fetched, size, r -> String.valueOf(r.getId()));
        Map<Long, String> nicknames = memberApi.findNicknames(page.items().stream().map(Review::getUserId).distinct().toList());
        List<ReviewResponse> items = page.items().stream()
                .map(r -> ReviewResponse.of(r, nicknames.get(r.getUserId()), viewerId))
                .toList();
        return new CursorPage<>(items, page.nextCursor());
    }

    @Transactional(readOnly = true)
    public ReviewResponse get(Long viewerId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .filter(Review::isVisible)
                .orElseThrow(() -> new ApiException(ReviewErrorCode.REVIEW_NOT_FOUND));
        return toResponse(review, viewerId);
    }

    /** 같은 달 후기가 이미 있으면 409 + details.reviewId. 삭제했던 같은 달 후기는 새 내용으로 다시 쓴다. */
    @Transactional
    public ReviewResponse create(Long userId, Long placeId, ReviewRequest request) {
        placeApi.requireVisible(placeId);
        ReviewContent content = toContent(request);

        Review review = reviewRepository.findByPlaceIdAndUserIdAndVisitedOn(placeId, userId, content.visitedOn())
                .map(existing -> {
                    if (!existing.isDeleted()) {
                        throw alreadyExists(existing.getId());
                    }
                    existing.rewrite(content);
                    return existing;
                })
                .orElseGet(() -> Review.write(placeId, userId, content));
        saveAndFlush(review);

        placeApi.applyReviewChange(placeId, 1, review.getRating());
        return toResponse(review, userId);
    }

    @Transactional
    public ReviewResponse update(Long userId, Long reviewId, ReviewRequest request) {
        Review review = getOwned(userId, reviewId);
        ReviewContent content = toContent(request);

        if (!content.visitedOn().equals(review.getVisitedOn())) {
            reviewRepository.findByPlaceIdAndUserIdAndVisitedOn(review.getPlaceId(), userId, content.visitedOn())
                    .ifPresent(other -> {
                        if (!other.isDeleted()) {
                            throw alreadyExists(other.getId());
                        }
                        // 삭제된 후기가 그 달 자리를 차지하고 있으면 지운다 (유니크 키: 장소·회원·방문 월)
                        reviewRepository.delete(other);
                        reviewRepository.flush();
                    });
        }

        int ratingDelta = content.rating() - review.getRating();
        review.rewrite(content);
        saveAndFlush(review);

        placeApi.applyReviewChange(review.getPlaceId(), 0, ratingDelta);
        return toResponse(review, userId);
    }

    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = getOwned(userId, reviewId);
        review.delete();
        reviewRepository.flush();
        placeApi.applyReviewChange(review.getPlaceId(), -1, -review.getRating());
    }

    private Review getOwned(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .filter(Review::isVisible)
                .orElseThrow(() -> new ApiException(ReviewErrorCode.REVIEW_NOT_FOUND));
        if (!review.isWrittenBy(userId)) {
            throw new ApiException(CommonErrorCode.FORBIDDEN);
        }
        return review;
    }

    private void saveAndFlush(Review review) {
        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            // 같은 달 후기를 동시에 두 번 보낸 경우
            throw new ApiException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    private ReviewContent toContent(ReviewRequest request) {
        ReviewContent content = new ReviewContent(YearMonth.parse(request.visitedOn()), request.childAgeGroup(),
                request.rating(), request.content(), request.amenities());
        content.requireNotFuture(YearMonth.now(SERVICE_ZONE));
        return content;
    }

    private ReviewResponse toResponse(Review review, Long viewerId) {
        String nickname = memberApi.findNicknames(List.of(review.getUserId())).get(review.getUserId());
        return ReviewResponse.of(review, nickname, viewerId);
    }

    private static ApiException alreadyExists(Long reviewId) {
        return new ApiException(ReviewErrorCode.REVIEW_ALREADY_EXISTS, ReviewErrorCode.REVIEW_ALREADY_EXISTS.getMessage(),
                Map.of("reviewId", reviewId));
    }

    private static Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new ApiException(CommonErrorCode.INVALID_REQUEST, "cursor 값이 올바르지 않습니다.");
        }
    }
}
