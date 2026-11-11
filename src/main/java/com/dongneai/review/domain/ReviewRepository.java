package com.dongneai.review.domain;

import java.time.YearMonth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 삭제된 후기도 포함한다 (DB 유니크 키가 장소·회원·방문 월 기준이므로). */
    Optional<Review> findByPlaceIdAndUserIdAndVisitedOn(Long placeId, Long userId, YearMonth visitedOn);
}
