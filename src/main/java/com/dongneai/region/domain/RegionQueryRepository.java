package com.dongneai.region.domain;

import static com.dongneai.region.domain.QRegion.region;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RegionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** 서비스 오픈된 읍면동 중 이름에 검색어가 포함된 지역. */
    public List<Region> searchActiveDongs(String keyword, int limit) {
        return queryFactory
                .selectFrom(region)
                .where(
                        region.active.isTrue(),
                        region.level.eq(Region.LEVEL_DONG),
                        region.name.contains(keyword).or(region.fullName.contains(keyword))
                )
                .orderBy(region.name.asc())
                .limit(limit)
                .fetch();
    }
}
