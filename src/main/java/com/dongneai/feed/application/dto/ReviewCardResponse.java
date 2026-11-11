package com.dongneai.feed.application.dto;

import com.dongneai.shared.code.AgeGroup;
import java.time.LocalDateTime;

/** 피드에 섞어 보여 주는 새 후기 카드. */
public record ReviewCardResponse(
        Long id,
        PlaceTag place,
        Author author,
        int rating,
        String preview,
        AgeGroup childAgeGroup,
        String visitedOn,
        LocalDateTime createdAt
) {
}
