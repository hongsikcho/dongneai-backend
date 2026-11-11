package com.dongneai.feed.application.dto;

import com.dongneai.feed.domain.Post;
import com.dongneai.feed.domain.PostCategory;
import com.dongneai.shared.code.AgeGroup;
import java.time.LocalDateTime;

/** 피드·장소 '이 장소 이야기' 목록의 글 카드. preview는 본문 앞부분. */
public record PostSummaryResponse(
        Long id,
        PostCategory category,
        String title,
        String preview,
        Author author,
        PlaceTag place,
        AgeGroup ageGroup,
        int reactionCount,
        int commentCount,
        boolean liked,
        LocalDateTime createdAt
) {
    private static final int PREVIEW_LENGTH = 100;

    public static PostSummaryResponse of(Post post, Author author, PlaceTag place, boolean liked) {
        String content = post.getContent();
        String preview = content.length() <= PREVIEW_LENGTH ? content : content.substring(0, PREVIEW_LENGTH) + "…";
        return new PostSummaryResponse(post.getId(), post.getCategory(), post.getTitle(), preview, author, place,
                post.getAgeGroup(), post.getReactionCount(), post.getCommentCount(), liked, post.getCreatedAt());
    }
}
