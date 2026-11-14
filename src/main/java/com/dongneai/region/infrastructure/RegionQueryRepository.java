package com.dongneai.region.infrastructure;

import static com.dongneai.region.domain.QRegion.region;

import com.dongneai.region.domain.Region;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RegionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** 서비스 오픈된 지역(시·구·동) 중 이름에 검색어가 포함된 곳. */
    public List<Region> searchActive(String keyword, int limit) {
        return queryFactory
                .selectFrom(region)
                .where(
                        region.active.isTrue(),
                        region.name.contains(keyword).or(region.fullName.contains(keyword))
                )
                .orderBy(region.name.asc())
                .limit(limit)
                .fetch();
    }
}
