package com.dongneai.member.application;

import com.dongneai.common.exception.ApiException;
import com.dongneai.common.exception.ErrorCode;
import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.TermsCode;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserAgreement;
import com.dongneai.member.domain.UserAgreementRepository;
import com.dongneai.member.domain.UserRepository;
import com.dongneai.member.dto.AgreementRequest;
import com.dongneai.member.dto.MeResponse;
import com.dongneai.member.dto.TermsResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final UserRepository userRepository;
    private final UserAgreementRepository agreementRepository;

    public List<TermsResponse> currentTerms() {
        return Arrays.stream(TermsCode.values()).map(TermsResponse::from).toList();
    }

    /** AUTH-03: 약관 동의 저장. 필수 약관 미동의 시 400. TERMS 단계면 PROFILE로 진행. */
    @Transactional
    public MeResponse agree(Long userId, AgreementRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Map<TermsCode, Boolean> answers = request.agreements().stream()
                .collect(Collectors.toMap(AgreementRequest.Item::code, AgreementRequest.Item::agreed, (a, b) -> b));
        boolean requiredMissing = Arrays.stream(TermsCode.values())
                .filter(TermsCode::isRequired)
                .anyMatch(code -> !answers.getOrDefault(code, false));
        if (requiredMissing) {
            throw new ApiException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        LocalDateTime now = LocalDateTime.now();
        String version = TermsCode.CURRENT_VERSION;
        answers.forEach((code, agreed) -> agreementRepository
                .findByUserIdAndTermsCodeAndTermsVersion(userId, code, version)
                .ifPresentOrElse(
                        existing -> existing.change(agreed, now),
                        () -> agreementRepository.save(new UserAgreement(userId, code, version, agreed, now))));

        user.completeOnboardingStep(OnboardingStep.TERMS);
        return MeResponse.from(user);
    }
}
