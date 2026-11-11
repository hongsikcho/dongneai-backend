package com.dongneai.review.domain;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 삭제된 후기도 포함한다 (DB 유니크 키가 장소·회원·방문 월 기준이므로). */
    Optional<Review> findByPlaceIdAndUserIdAndVisitedOn(Long placeId, Long userId, YearMonth visitedOn);

    /** 이용 조건 집계용. 체크는 배치 페치(default_batch_fetch_size)로 함께 읽는다. */
    List<Review> findByPlaceIdAndStatus(Long placeId, ReviewStatus status);
}
