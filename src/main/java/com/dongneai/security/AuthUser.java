package com.dongneai.security;

/**
 * 인증된 요청의 principal. 컨트롤러에서 {@code @AuthenticationPrincipal AuthUser user}로 받는다.
 */
public record AuthUser(Long userId, String role) {
}
