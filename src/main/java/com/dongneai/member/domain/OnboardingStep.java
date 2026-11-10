package com.dongneai.member.domain;

/** 가입 절차. TERMS → PROFILE → REGION → DONE 순서로만 진행한다. */
public enum OnboardingStep {
    TERMS, PROFILE, REGION, DONE;

    public OnboardingStep next() {
        return this == DONE ? DONE : values()[ordinal() + 1];
    }

    public boolean isBefore(OnboardingStep other) {
        return ordinal() < other.ordinal();
    }
}
