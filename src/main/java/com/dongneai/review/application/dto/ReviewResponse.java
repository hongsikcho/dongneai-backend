package com.dongneai.review.application.dto;

import com.dongneai.review.domain.Review;
import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * 후기. author.nickname은 프로필 설정 전이거나 탈퇴한 회원이면 null (앱에서 '이웃'으로 표시).
 * edited는 작성 후 수정된 적이 있는지, mine은 조회한 회원이 쓴 후기인지.
 */
public record ReviewResponse(
        Long id,
        Long placeId,
        Author author,
        String visitedOn,
        AgeGroup childAgeGroup,
        int rating,
        String content,
        Map<Amenity, AmenityStatus> amenities,
        LocalDateTime createdAt,
        boolean edited,
        boolean mine
) {
    public record Author(Long id, String nickname) {
    }

    public static ReviewResponse of(Review review, String nickname, Long viewerId) {
        Map<Amenity, AmenityStatus> amenities = new EnumMap<>(Amenity.class);
        amenities.putAll(review.getAmenityChecks());
        boolean edited = review.getUpdatedAt() != null && review.getCreatedAt() != null
                && review.getUpdatedAt().isAfter(review.getCreatedAt().plusSeconds(1));
        return new ReviewResponse(review.getId(), review.getPlaceId(), new Author(review.getUserId(), nickname),
                review.getVisitedOn().toString(), review.getChildAgeGroup(), review.getRating(), review.getContent(),
                amenities, review.getCreatedAt(), edited, review.isWrittenBy(viewerId));
    }
}
