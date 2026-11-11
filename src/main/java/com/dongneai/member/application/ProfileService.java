package com.dongneai.member.application;

import com.dongneai.member.application.dto.MeResponse;
import com.dongneai.member.application.dto.NicknameCheckResponse;
import com.dongneai.member.application.dto.UpdateProfileRequest;
import com.dongneai.member.domain.MemberErrorCode;
import com.dongneai.member.domain.NicknamePolicy;
import com.dongneai.member.domain.OnboardingStep;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserRepository;
import com.dongneai.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        return MeResponse.from(getUser(userId));
    }

    /** AUTH-04, MY-03: 닉네임 사용 가능 여부. 내 현재 닉네임은 사용 가능으로 본다. */
    @Transactional(readOnly = true)
    public NicknameCheckResponse checkNickname(Long userId, String nickname) {
        String value = nickname == null ? null : nickname.strip();
        return switch (NicknamePolicy.check(value)) {
            case INVALID_FORMAT -> NicknameCheckResponse.unavailable("INVALID_FORMAT");
            case FORBIDDEN_WORD -> NicknameCheckResponse.unavailable("FORBIDDEN_WORD");
            case OK -> userRepository.existsByNicknameAndIdNot(value, userId)
                    ? NicknameCheckResponse.unavailable("DUPLICATED")
                    : NicknameCheckResponse.ok();
        };
    }

    /** AUTH-04, MY-03: 프로필 수정. 약관 동의 전이면 403, PROFILE 단계면 REGION으로 진행. */
    @Transactional
    public MeResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        if (!user.hasReached(OnboardingStep.PROFILE)) {
            throw new ApiException(MemberErrorCode.ONBOARDING_REQUIRED);
        }

        String nickname = request.nickname() == null ? user.getNickname() : request.nickname().strip();
        var ageGroup = request.childAgeGroup() == null ? user.getChildAgeGroup() : request.childAgeGroup();

        if (request.nickname() != null) {
            validateNickname(userId, nickname);
        }
        if (user.getOnboardingStep() == OnboardingStep.PROFILE && (nickname == null || ageGroup == null)) {
            throw new ApiException(MemberErrorCode.PROFILE_INCOMPLETE);
        }

        user.updateProfile(nickname, ageGroup);
        user.completeOnboardingStep(OnboardingStep.PROFILE);
        try {
            userRepository.flush(); // 동시 가입으로 인한 닉네임 UNIQUE 위반을 여기서 잡는다
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(MemberErrorCode.NICKNAME_DUPLICATED);
        }
        return MeResponse.from(user);
    }

    private void validateNickname(Long userId, String nickname) {
        switch (NicknamePolicy.check(nickname)) {
            case INVALID_FORMAT -> throw new ApiException(MemberErrorCode.INVALID_NICKNAME);
            case FORBIDDEN_WORD -> throw new ApiException(MemberErrorCode.FORBIDDEN_NICKNAME);
            case OK -> {
                if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
                    throw new ApiException(MemberErrorCode.NICKNAME_DUPLICATED);
                }
            }
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));
    }
}
