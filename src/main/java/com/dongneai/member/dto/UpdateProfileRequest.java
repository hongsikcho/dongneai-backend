package com.dongneai.member.dto;

import com.dongneai.common.code.AgeGroup;

/** PATCH: null인 항목은 바꾸지 않는다. 온보딩 PROFILE 단계에서는 두 값이 모두 채워져야 한다. */
public record UpdateProfileRequest(String nickname, AgeGroup childAgeGroup) {
}
