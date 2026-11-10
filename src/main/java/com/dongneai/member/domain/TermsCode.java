package com.dongneai.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 약관 종류. 약관 본문이 바뀌면 {@link #CURRENT_VERSION}을 올리고 재동의를 받는다.
 * 위치정보는 생활권(행정동) 수준만 받으므로 위치기반서비스 약관은 두지 않는다 (법률 검토 시 재확인).
 */
@Getter
@RequiredArgsConstructor
public enum TermsCode {
    SERVICE("서비스 이용약관", true),
    PRIVACY("개인정보 수집·이용 동의", true),
    MARKETING("이벤트·혜택 알림 수신 동의", false);

    public static final String CURRENT_VERSION = "1.0";

    private final String title;
    private final boolean required;
}
