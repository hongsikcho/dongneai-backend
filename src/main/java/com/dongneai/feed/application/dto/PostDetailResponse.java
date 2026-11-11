package com.dongneai.feed.application.dto;

import com.dongneai.feed.domain.Post;
import com.dongneai.feed.domain.PostCategory;
import com.dongneai.shared.code.AgeGroup;
import java.time.LocalDateTime;

/** FEED-02 글 상세. */
public record PostDetailResponse(
        Long id,
        PostCategory category,
        String title,
        String content,
        Author author,
        RegionRef region,
        PlaceTag place,
        AgeGroup ageGroup,
        int reactionCount,
        int commentCount,
        boolean liked,
        boolean mine,
        boolean edited,
        LocalDateTime createdAt
) {
    public record RegionRef(Long id, String name) {
    }

    public static PostDetailResponse of(Post post, Author author, RegionRef region, PlaceTag place, boolean liked,
                                        Long viewerId) {
        boolean edited = post.getUpdatedAt() != null && post.getCreatedAt() != null
                && post.getUpdatedAt().isAfter(post.getCreatedAt().plusSeconds(1));
        return new PostDetailResponse(post.getId(), post.getCategory(), post.getTitle(), post.getContent(), author,
                region, place, post.getAgeGroup(), post.getReactionCount(), post.getCommentCount(), liked,
                post.isWrittenBy(viewerId), edited, post.getCreatedAt());
    }
}
