package com.dongneai.member.domain;

import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "child_age_group", length = 20)
    private AgeGroup childAgeGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_step", nullable = false, length = 20)
    private OnboardingStep onboardingStep;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    private User(Provider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
        this.onboardingStep = OnboardingStep.TERMS;
    }

    public static User signUp(Provider provider, String providerId) {
        return new User(provider, providerId);
    }

    public void updateProfile(String nickname, AgeGroup childAgeGroup) {
        this.nickname = nickname;
        this.childAgeGroup = childAgeGroup;
    }

    /** 현재 단계가 {@code completed}일 때만 다음 단계로 넘어간다. 이미 지난 단계를 다시 해도 되돌아가지 않는다. */
    public void completeOnboardingStep(OnboardingStep completed) {
        if (this.onboardingStep == completed) {
            this.onboardingStep = completed.next();
        }
    }

    public boolean hasReached(OnboardingStep step) {
        return !onboardingStep.isBefore(step);
    }

    /** 로컬 개발용 관리자 승격 (DevAuthController 전용) */
    public void grantAdmin() {
        this.role = UserRole.ADMIN;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
