package com.dongneai.member.presentation;

import com.dongneai.member.application.dto.MeResponse;
import com.dongneai.member.domain.MemberErrorCode;
import com.dongneai.member.domain.UserRepository;
import com.dongneai.security.AuthUser;
import com.dongneai.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API. 카카오·Apple OAuth(POST /oauth/{provider}), refresh, logout은 다음 단계에서 추가한다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    /** AUTH-01 스플래시: 내 상태와 온보딩 단계. */
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public MeResponse me(@AuthenticationPrincipal AuthUser authUser) {
        return userRepository.findById(authUser.userId())
                .map(MeResponse::from)
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));
    }
}
