package com.dongneai.member.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, UserAgreementId> {

    Optional<UserAgreement> findByUserIdAndTermsCodeAndTermsVersion(Long userId, TermsCode termsCode, String termsVersion);
}
