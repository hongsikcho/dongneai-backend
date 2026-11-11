package com.dongneai.feed.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, ReactionId> {

    @Query("select r.targetId from Reaction r where r.userId = :userId "
            + "and r.targetType = com.dongneai.feed.domain.ReactionTarget.POST and r.type = 'LIKE' and r.targetId in :postIds")
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
