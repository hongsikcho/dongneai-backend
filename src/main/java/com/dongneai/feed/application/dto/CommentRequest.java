package com.dongneai.feed.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 작성. parentId가 있으면 그 댓글의 답글. */
public record CommentRequest(@NotBlank @Size(max = 1000) String content, Long parentId) {
}
