package com.dongneai.member.domain;

import com.dongneai.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 회원·약관·프로필·온보딩 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해 주세요."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 한글·영문·숫자 2~10자로 입력해 주세요."),
    FORBIDDEN_NICKNAME(HttpStatus.BAD_REQUEST, "사용할 수 없는 닉네임입니다."),
    PROFILE_INCOMPLETE(HttpStatus.BAD_REQUEST, "닉네임과 자녀 연령대를 모두 입력해 주세요."),
    ONBOARDING_REQUIRED(HttpStatus.FORBIDDEN, "가입 절차를 먼저 완료해 주세요."),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "이용이 제한된 계정입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");

    private final HttpStatus status;
    private final String message;
}
