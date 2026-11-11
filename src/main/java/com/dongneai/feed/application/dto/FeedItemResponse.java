package com.dongneai.feed.application.dto;

import java.time.LocalDateTime;

/** 홈 피드 항목. type이 POST면 post, REVIEW면 review가 채워진다. */
public record FeedItemResponse(String type, LocalDateTime createdAt, PostSummaryResponse post,
                               ReviewCardResponse review) {

    public static FeedItemResponse post(PostSummaryResponse post) {
        return new FeedItemResponse("POST", post.createdAt(), post, null);
    }

    public static FeedItemResponse review(ReviewCardResponse review) {
        return new FeedItemResponse("REVIEW", review.createdAt(), null, review);
    }
}
