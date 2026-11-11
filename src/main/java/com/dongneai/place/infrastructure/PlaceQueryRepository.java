package com.dongneai.place.infrastructure;

import static com.dongneai.place.domain.QPlace.place;
import static com.dongneai.place.domain.QPlaceAmenity.placeAmenity;

import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.AmenityStatus;
import com.dongneai.place.domain.Place;
import com.dongneai.place.domain.PlaceCursor;
import com.dongneai.place.domain.PlaceSearchCondition;
import com.dongneai.place.domain.PlaceSort;
import com.dongneai.place.domain.PlaceStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** 커서 페이징. 다음 페이지 여부 판단을 위해 size + 1건을 조회한다. */
    public List<Place> search(PlaceSearchCondition cond) {
        BooleanBuilder where = new BooleanBuilder();
        if (cond.regionId() != null) {
            where.and(place.regionId.eq(cond.regionId()));
        }
        if (cond.category() != null) {
            where.and(place.category.eq(cond.category()));
        }
        if (!cond.includeHidden()) {
            where.and(place.status.eq(PlaceStatus.VISIBLE));
        }
        if (cond.keyword() != null && !cond.keyword().isBlank()) {
            // 초기 데이터 규모에서는 LIKE로 충분. 데이터가 늘면 FULLTEXT(ngram) 검색으로 교체한다.
            where.and(place.name.contains(cond.keyword().strip()));
        }
        if (cond.amenities() != null) {
            for (Amenity amenity : cond.amenities()) {
                where.and(JPAExpressions.selectOne()
                        .from(placeAmenity)
                        .where(placeAmenity.placeId.eq(place.id),
                                placeAmenity.amenity.eq(amenity),
                                placeAmenity.status.eq(AmenityStatus.YES))
                        .exists());
            }
        }

        PlaceCursor cursor = cond.cursor();
        List<OrderSpecifier<?>> orders;
        if (cond.sort() == PlaceSort.REVIEWS) {
            if (cursor != null) {
                where.and(place.reviewCount.lt(cursor.reviewCount())
                        .or(place.reviewCount.eq(cursor.reviewCount()).and(place.id.lt(cursor.id()))));
            }
            orders = List.of(place.reviewCount.desc(), place.id.desc());
        } else {
            if (cursor != null) {
                where.and(place.id.lt(cursor.id()));
            }
            orders = List.of(place.id.desc());
        }

        return queryFactory.selectFrom(place)
                .where(where)
                .orderBy(orders.toArray(OrderSpecifier[]::new))
                .limit(cond.size() + 1L)
                .fetch();
    }
}
