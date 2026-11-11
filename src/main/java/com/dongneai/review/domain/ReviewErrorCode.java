package com.dongneai.review.domain;

import com.dongneai.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 후기 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    INVALID_VISIT_MONTH(HttpStatus.BAD_REQUEST, "방문 월은 이번 달까지만 선택할 수 있어요."),
    INVALID_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "후기는 10자 이상 1000자 이하로 써 주세요."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이 장소에 같은 달 방문 후기를 이미 썼어요.");

    private final HttpStatus status;
    private final String message;
}
