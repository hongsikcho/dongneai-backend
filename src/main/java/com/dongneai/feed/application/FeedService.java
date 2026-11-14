package com.dongneai.feed.application;

import com.dongneai.feed.application.dto.Author;
import com.dongneai.feed.application.dto.FeedItemResponse;
import com.dongneai.feed.application.dto.PostSummaryResponse;
import com.dongneai.feed.application.dto.ReviewCardResponse;
import com.dongneai.feed.domain.FeedTimeline;
import com.dongneai.feed.domain.Post;
import com.dongneai.feed.domain.PostCategory;
import com.dongneai.feed.infrastructure.PostQueryRepository;
import com.dongneai.place.api.PlaceApi;
import com.dongneai.region.api.RegionApi;
import com.dongneai.review.api.ReviewApi;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.exception.CommonErrorCode;
import com.dongneai.shared.response.CursorPage;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FEED-01 홈 피드: 동네 글 + (전체 카테고리일 때) 그 동네 장소의 새 후기 카드. */
@Service
@RequiredArgsConstructor
public class FeedService {

    private static final int REVIEW_PREVIEW_LENGTH = 80;

    private final PostQueryRepository postQueryRepository;
    private final FeedAssembler assembler;
    private final RegionApi regionApi;
    private final PlaceApi placeApi;
    private final ReviewApi reviewApi;

    @Transactional(readOnly = true)
    public CursorPage<FeedItemResponse> feed(Long viewerId, Long regionId, PostCategory category, String cursor, int size) {
        regionApi.requireOpenRegion(regionId);
        LocalDateTime before = parseCursor(cursor);

        List<Post> posts = postQueryRepository.findFeed(regionId, category, before, size + 1);
        List<ReviewApi.ReviewSummary> reviews = category == null
                ? reviewApi.findRecentByPlaces(placeApi.findVisiblePlaceIds(regionId), before, size + 1)
                : List.of();

        List<FeedTimeline.Entry<Object>> entries = new ArrayList<>();
        posts.forEach(p -> entries.add(new FeedTimeline.Entry<>(p.getCreatedAt(), p)));
        reviews.forEach(r -> entries.add(new FeedTimeline.Entry<>(r.createdAt(), r)));
        FeedTimeline.Page<Object> page = FeedTimeline.merge(entries, size);

        List<Post> pagePosts = page.items().stream().filter(Post.class::isInstance).map(Post.class::cast).toList();
        List<ReviewApi.ReviewSummary> pageReviews = page.items().stream()
                .filter(ReviewApi.ReviewSummary.class::isInstance).map(ReviewApi.ReviewSummary.class::cast).toList();

        Map<Long, PostSummaryResponse> postCards = new HashMap<>();
        assembler.summaries(pagePosts, viewerId).forEach(c -> postCards.put(c.id(), c));
        Map<Long, String> reviewers = assembler.nicknames(pageReviews.stream().map(ReviewApi.ReviewSummary::userId).toList());
        Map<Long, PlaceApi.PlaceRef> reviewPlaces = assembler.places(pageReviews.stream().map(ReviewApi.ReviewSummary::placeId).toList());

        List<FeedItemResponse> items = page.items().stream().map(item -> {
            if (item instanceof Post p) {
                return FeedItemResponse.post(postCards.get(p.getId()));
            }
            ReviewApi.ReviewSummary r = (ReviewApi.ReviewSummary) item;
            String preview = r.content().length() <= REVIEW_PREVIEW_LENGTH
                    ? r.content() : r.content().substring(0, REVIEW_PREVIEW_LENGTH) + "…";
            return FeedItemResponse.review(new ReviewCardResponse(r.id(), FeedAssembler.tag(reviewPlaces.get(r.placeId())),
                    new Author(r.userId(), reviewers.get(r.userId())), r.rating(), preview, r.childAgeGroup(),
                    r.visitedOn(), r.createdAt()));
        }).toList();
        return new CursorPage<>(items, page.nextCursor());
    }

    private static LocalDateTime parseCursor(String cursor) {
        try {
            return FeedTimeline.parseCursor(cursor);
        } catch (DateTimeParseException e) {
            throw new ApiException(CommonErrorCode.INVALID_REQUEST, "cursor 값이 올바르지 않습니다.");
        }
    }
}
