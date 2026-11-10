package com.dongneai.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.Provider;
import com.dongneai.member.domain.User;
import org.junit.jupiter.api.Test;

class OnboardingStepTest {

    @Test
    void 현재_단계를_완료하면_다음_단계로_간다() {
        User user = User.signUp(Provider.DEV, "u1");

        user.completeOnboardingStep(OnboardingStep.TERMS);
        user.completeOnboardingStep(OnboardingStep.PROFILE);
        user.completeOnboardingStep(OnboardingStep.REGION);

        assertThat(user.getOnboardingStep()).isEqualTo(OnboardingStep.DONE);
    }

    @Test
    void 이미_지난_단계를_다시_완료해도_되돌아가지_않는다() {
        User user = User.signUp(Provider.DEV, "u1");
        user.completeOnboardingStep(OnboardingStep.TERMS);
        user.completeOnboardingStep(OnboardingStep.PROFILE);

        user.completeOnboardingStep(OnboardingStep.TERMS);

        assertThat(user.getOnboardingStep()).isEqualTo(OnboardingStep.REGION);
        assertThat(user.hasReached(OnboardingStep.PROFILE)).isTrue();
        assertThat(user.hasReached(OnboardingStep.DONE)).isFalse();
    }

    @Test
    void 순서를_건너뛸_수_없다() {
        User user = User.signUp(Provider.DEV, "u1");

        user.completeOnboardingStep(OnboardingStep.PROFILE);

        assertThat(user.getOnboardingStep()).isEqualTo(OnboardingStep.TERMS);
    }
}
