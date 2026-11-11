package com.dongneai.place.domain;

import com.dongneai.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 장소 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 장소입니다."),
    EXTERNAL_SEARCH_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "장소 검색을 지금 사용할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
