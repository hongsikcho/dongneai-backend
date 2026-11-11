package com.dongneai.member.api;

/** 다른 모듈이 회원 상태를 확인하거나 온보딩을 진행할 때 쓰는 공개 API. */
public interface MemberApi {

    /**
     * 관심 지역을 설정할 수 있는 회원인지 확인한다 (프로필 설정까지 마친 상태).
     * 회원이 없으면 USER_NOT_FOUND, 아직 이전 단계면 ONBOARDING_REQUIRED.
     */
    void requireRegionSetupAllowed(Long userId);

    /** 관심 지역 설정 완료를 온보딩에 반영한다. REGION 단계였으면 DONE으로 넘어간다. */
    void completeRegionSetup(Long userId);
}
