package com.dongneai.member.application;

import com.dongneai.member.api.MemberApi;
import com.dongneai.member.domain.MemberErrorCode;
import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserRepository;
import com.dongneai.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberApiService implements MemberApi {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public void requireRegionSetupAllowed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));
        if (!user.hasReached(OnboardingStep.REGION)) {
            throw new ApiException(MemberErrorCode.ONBOARDING_REQUIRED);
        }
    }

    @Override
    @Transactional
    public void completeRegionSetup(Long userId) {
        // 호출하는 쪽에서 벌크 삭제로 영속성 컨텍스트가 비워졌을 수 있으므로 회원을 다시 읽어 단계를 갱신한다
        userRepository.findById(userId).ifPresent(u -> u.completeOnboardingStep(OnboardingStep.REGION));
    }
}
