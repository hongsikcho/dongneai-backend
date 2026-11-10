package com.dongneai.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해 주세요."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 한글·영문·숫자 2~10자로 입력해 주세요."),
    FORBIDDEN_NICKNAME(HttpStatus.BAD_REQUEST, "사용할 수 없는 닉네임입니다."),
    PROFILE_INCOMPLETE(HttpStatus.BAD_REQUEST, "닉네임과 자녀 연령대를 모두 입력해 주세요."),
    INVALID_REGION_SELECTION(HttpStatus.BAD_REQUEST, "동네는 1~3개, 기본 동네는 1개만 선택할 수 있습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    ONBOARDING_REQUIRED(HttpStatus.FORBIDDEN, "가입 절차를 먼저 완료해 주세요."),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "이용이 제한된 계정입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 장소입니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "이미 신고한 대상입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 시도해 주세요."),
    EXTERNAL_SEARCH_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "장소 검색을 지금 사용할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
