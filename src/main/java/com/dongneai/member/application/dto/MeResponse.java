package com.dongneai.member.application.dto;

import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserRole;
import com.dongneai.member.domain.UserStatus;
import com.dongneai.shared.code.AgeGroup;

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
