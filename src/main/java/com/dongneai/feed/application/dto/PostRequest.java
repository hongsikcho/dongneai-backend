package com.dongneai.feed.application.dto;

import com.dongneai.feed.domain.PostCategory;
import com.dongneai.shared.code.AgeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** FEED-03 글 작성·수정. regionId는 작성 때만 쓰고(내 관심 동네), 수정 때는 무시한다. */
public record PostRequest(
        Long regionId,
        @NotNull PostCategory category,
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 4000) String content,
        AgeGroup ageGroup,
        Long placeId
) {
}
