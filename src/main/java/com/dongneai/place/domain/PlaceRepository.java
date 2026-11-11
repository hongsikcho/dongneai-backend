package com.dongneai.place.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);

    List<Place> findByKakaoPlaceIdIn(List<String> kakaoPlaceIds);

    /**
     * 후기 수·별점 합계를 원자적으로 증감한다 (동시 작성에도 값이 어긋나지 않게).
     * 같은 트랜잭션의 후기 엔티티가 detach되지 않도록 영속성 컨텍스트는 비우지 않는다.
     */
    @Modifying(flushAutomatically = true)
    @Query("update Place p set p.reviewCount = p.reviewCount + :countDelta, p.ratingSum = p.ratingSum + :ratingDelta "
            + "where p.id = :placeId")
    int applyReviewChange(@Param("placeId") Long placeId, @Param("countDelta") int countDelta,
                          @Param("ratingDelta") int ratingDelta);

    @Modifying(flushAutomatically = true)
    @Query("update Place p set p.postCount = p.postCount + :countDelta where p.id = :placeId")
    int applyPostChange(@Param("placeId") Long placeId, @Param("countDelta") int countDelta);

    @Query("select p.id from Place p where p.regionId = :regionId and p.status = com.dongneai.place.domain.PlaceStatus.VISIBLE")
    List<Long> findVisibleIdsByRegionId(@Param("regionId") Long regionId);
}
