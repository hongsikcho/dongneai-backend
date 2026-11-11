package com.dongneai.feed.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 글의 댓글 전체 (삭제된 것 포함, 작성순). 삭제된 댓글은 답글이 있을 때만 자리 표시로 보여 준다. */
    List<Comment> findByPostIdOrderByIdAsc(Long postId);
}
