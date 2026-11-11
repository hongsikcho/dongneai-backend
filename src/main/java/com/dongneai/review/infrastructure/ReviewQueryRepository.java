package com.dongneai.review.infrastructure;

import static com.dongneai.review.domain.QReview.review;

import com.dongneai.review.domain.Review;
import com.dongneai.review.domain.ReviewStatus;
import com.dongneai.shared.code.AgeGroup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** 장소의 공개 후기, 최신순 커서 페이징. 다음 페이지 판단을 위해 size + 1건을 조회한다. */
    public List<Review> findVisibleByPlace(Long placeId, AgeGroup ageGroup, Long cursorId, int size) {
        BooleanBuilder where = new BooleanBuilder()
                .and(review.placeId.eq(placeId))
                .and(review.status.eq(ReviewStatus.VISIBLE));
        if (ageGroup != null) {
            where.and(review.childAgeGroup.eq(ageGroup));
        }
        if (cursorId != null) {
            where.and(review.id.lt(cursorId));
        }
        return queryFactory.selectFrom(review)
                .where(where)
                .orderBy(review.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    /** 여러 장소의 공개 후기, 작성 시각 최신순 (피드 카드용). */
    public List<Review> findRecentVisibleByPlaces(Collection<Long> placeIds, LocalDateTime before, int limit) {
        BooleanBuilder where = new BooleanBuilder()
                .and(review.placeId.in(placeIds))
                .and(review.status.eq(ReviewStatus.VISIBLE));
        if (before != null) {
            where.and(review.createdAt.lt(before));
        }
        return queryFactory.selectFrom(review)
                .where(where)
                .orderBy(review.createdAt.desc(), review.id.desc())
                .limit(limit)
                .fetch();
    }
}
