package com.dongneai.member.api;

import java.util.Collection;
import java.util.Map;

/** 다른 모듈이 회원 상태를 확인하거나 온보딩을 진행할 때 쓰는 공개 API. */
public interface MemberApi {

    /**
     * 관심 지역을 설정할 수 있는 회원인지 확인한다 (프로필 설정까지 마친 상태).
     * 회원이 없으면 USER_NOT_FOUND, 아직 이전 단계면 ONBOARDING_REQUIRED.
     */
    void requireRegionSetupAllowed(Long userId);

    /** 관심 지역 설정 완료를 온보딩에 반영한다. REGION 단계였으면 DONE으로 넘어간다. */
    void completeRegionSetup(Long userId);

    /** 표시용 닉네임 일괄 조회. 닉네임이 없거나(프로필 설정 전) 탈퇴한 회원은 결과에서 빠진다. */
    Map<Long, String> findNicknames(Collection<Long> userIds);
}
