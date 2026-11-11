package com.dongneai.region.domain;

import com.dongneai.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 지역·관심 지역 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum RegionErrorCode implements ErrorCode {

    INVALID_REGION_SELECTION(HttpStatus.BAD_REQUEST, "동네는 1~3개, 기본 동네는 1개만 선택할 수 있습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
