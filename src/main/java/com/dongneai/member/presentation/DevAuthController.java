package com.dongneai.member.presentation;

import com.dongneai.member.domain.Provider;
import com.dongneai.member.domain.User;
import com.dongneai.member.domain.UserRepository;
import com.dongneai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로컬 개발용 토큰 발급. 소셜 로그인 연동 전까지 앱·API 테스트에 사용한다.
 * local 프로필에서만 빈으로 등록되므로 운영에는 노출되지 않는다.
 * admin=true 로 호출하면 관리자 권한 토큰을 받는다 (장소 시딩 테스트용).
 */
@Profile("local")
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/token")
    @Transactional
    public DevTokenResponse issue(@RequestParam(defaultValue = "dev-user-1") String devId,
                                  @RequestParam(defaultValue = "false") boolean admin) {
        User user = userRepository.findByProviderAndProviderId(Provider.DEV, devId)
                .orElseGet(() -> userRepository.save(User.signUp(Provider.DEV, devId)));
        if (admin) {
            user.grantAdmin();
        }
        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getRole().name());
        return new DevTokenResponse(user.getId(), accessToken);
    }

    public record DevTokenResponse(Long userId, String accessToken) {
    }
}
