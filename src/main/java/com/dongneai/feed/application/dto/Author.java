package com.dongneai.feed.application.dto;

/** 작성자. nickname이 null이면 프로필 설정 전이거나 탈퇴한 회원 (앱에서 '이웃'으로 표시). */
public record Author(Long id, String nickname) {
}
