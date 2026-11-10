package com.dongneai.member.dto;

import com.dongneai.common.code.AgeGroup;
import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserRole;
import com.dongneai.member.domain.UserStatus;

public record MeResponse(
        Long id,
        String nickname,
        AgeGroup childAgeGroup,
        UserRole role,
        UserStatus status,
        OnboardingStep onboardingStep
) {
    public static MeResponse from(User user) {
        return new MeResponse(user.getId(), user.getNickname(), user.getChildAgeGroup(),
                user.getRole(), user.getStatus(), user.getOnboardingStep());
    }
}
