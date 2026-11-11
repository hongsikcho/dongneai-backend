package com.dongneai.feed.domain;

import com.dongneai.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 피드(글·댓글) 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum FeedErrorCode implements ErrorCode {

    INVALID_POST(HttpStatus.BAD_REQUEST, "제목은 2~50자, 본문은 5~2000자로 써 주세요."),
    INVALID_COMMENT(HttpStatus.BAD_REQUEST, "댓글은 1~500자로 써 주세요."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "답글에는 답글을 달 수 없어요."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
