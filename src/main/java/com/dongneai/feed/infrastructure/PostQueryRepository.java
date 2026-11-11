package com.dongneai.feed.infrastructure;

import static com.dongneai.feed.domain.QPost.post;

import com.dongneai.feed.domain.Post;
import com.dongneai.feed.domain.PostCategory;
import com.dongneai.feed.domain.PostStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** 동네 피드의 공개 글, 작성 시각 최신순. before가 있으면 그보다 이전 글만. */
    public List<Post> findFeed(Long regionId, PostCategory category, LocalDateTime before, int limit) {
        BooleanBuilder where = new BooleanBuilder()
                .and(post.regionId.eq(regionId))
                .and(post.status.eq(PostStatus.VISIBLE));
        if (category != null) {
            where.and(post.category.eq(category));
        }
        if (before != null) {
            where.and(post.createdAt.lt(before));
        }
        return queryFactory.selectFrom(post)
                .where(where)
                .orderBy(post.createdAt.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    /** 장소를 태그한 공개 글 ('이 장소 이야기'), 최신순 id 커서. */
    public List<Post> findByPlace(Long placeId, Long cursorId, int limit) {
        BooleanBuilder where = new BooleanBuilder()
                .and(post.placeId.eq(placeId))
                .and(post.status.eq(PostStatus.VISIBLE));
        if (cursorId != null) {
            where.and(post.id.lt(cursorId));
        }
        return queryFactory.selectFrom(post)
                .where(where)
                .orderBy(post.id.desc())
                .limit(limit)
                .fetch();
    }
}
