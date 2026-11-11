package com.dongneai.review.api;

import com.dongneai.shared.code.AgeGroup;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** 다른 모듈(피드)이 후기를 읽을 때 쓰는 공개 API. */
public interface ReviewApi {

    /** 주어진 장소들의 공개 후기, 작성 시각 최신순. before가 있으면 그보다 이전 것만. */
    List<ReviewSummary> findRecentByPlaces(Collection<Long> placeIds, LocalDateTime before, int limit);

    record ReviewSummary(Long id, Long placeId, Long userId, int rating, String content, AgeGroup childAgeGroup,
                         String visitedOn, LocalDateTime createdAt) {
    }
}
