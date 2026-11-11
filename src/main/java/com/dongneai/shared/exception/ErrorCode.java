package com.dongneai.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 계약. 각 모듈은 자기 에러를 이 인터페이스를 구현한 enum으로 둔다
 * (예: {@link CommonErrorCode}, member의 MemberErrorCode). 응답의 code 값은 enum 상수 이름이다.
 */
public interface ErrorCode {

    String name();

    HttpStatus getStatus();

    String getMessage();
}
