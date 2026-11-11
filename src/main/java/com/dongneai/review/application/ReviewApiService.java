package com.dongneai.review.application;

import com.dongneai.review.api.ReviewApi;
import com.dongneai.review.infrastructure.ReviewQueryRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewApiService implements ReviewApi {

    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewSummary> findRecentByPlaces(Collection<Long> placeIds, LocalDateTime before, int limit) {
        if (placeIds.isEmpty() || limit <= 0) {
            return List.of();
        }
        return reviewQueryRepository.findRecentVisibleByPlaces(placeIds, before, limit).stream()
                .map(r -> new ReviewSummary(r.getId(), r.getPlaceId(), r.getUserId(), r.getRating(), r.getContent(),
                        r.getChildAgeGroup(), r.getVisitedOn().toString(), r.getCreatedAt()))
                .toList();
    }
}
