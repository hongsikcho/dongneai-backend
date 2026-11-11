package com.dongneai.feed.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying(flushAutomatically = true)
    @Query("update Post p set p.commentCount = p.commentCount + :delta where p.id = :postId")
    int addCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    @Modifying(flushAutomatically = true)
    @Query("update Post p set p.reactionCount = p.reactionCount + :delta where p.id = :postId")
    int addReactionCount(@Param("postId") Long postId, @Param("delta") int delta);
}
