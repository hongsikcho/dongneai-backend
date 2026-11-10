package com.dongneai.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 약관 동의 이력. (회원, 약관, 버전)당 1건이며 동의 철회도 agreed=false로 남긴다. */
@Getter
@Entity
@Table(name = "user_agreements")
@IdClass(UserAgreementId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAgreement {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "terms_code", length = 30)
    private TermsCode termsCode;

    @Id
    @Column(name = "terms_version", length = 10)
    private String termsVersion;

    @Column(nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    public UserAgreement(Long userId, TermsCode termsCode, String termsVersion, boolean agreed, LocalDateTime agreedAt) {
        this.userId = userId;
        this.termsCode = termsCode;
        this.termsVersion = termsVersion;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
    }

    public void change(boolean agreed, LocalDateTime at) {
        this.agreed = agreed;
        this.agreedAt = at;
    }
}
