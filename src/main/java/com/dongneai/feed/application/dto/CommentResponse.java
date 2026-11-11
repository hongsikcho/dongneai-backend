package com.dongneai.feed.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글. 삭제된 댓글은 답글이 있을 때만 deleted=true, content=null로 자리를 남긴다.
 * replies는 최상위 댓글에만 채워진다.
 */
public record CommentResponse(
        Long id,
        Long parentId,
        Author author,
        String content,
        boolean deleted,
        boolean mine,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
}
