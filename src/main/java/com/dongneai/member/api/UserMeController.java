package com.dongneai.member.api;

import com.dongneai.member.application.ProfileService;
import com.dongneai.member.application.TermsService;
import com.dongneai.member.dto.AgreementRequest;
import com.dongneai.member.dto.MeResponse;
import com.dongneai.member.dto.NicknameCheckResponse;
import com.dongneai.member.dto.UpdateProfileRequest;
import com.dongneai.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserMeController {

    private final TermsService termsService;
    private final ProfileService profileService;

    /** MY-01 */
    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthUser user) {
        return profileService.me(user.userId());
    }

    /** AUTH-03 */
    @PostMapping("/me/agreements")
    public MeResponse agree(@AuthenticationPrincipal AuthUser user, @RequestBody @Valid AgreementRequest request) {
        return termsService.agree(user.userId(), request);
    }

    /** AUTH-04, MY-03 */
    @GetMapping("/nickname/check")
    public NicknameCheckResponse checkNickname(@AuthenticationPrincipal AuthUser user, @RequestParam String nickname) {
        return profileService.checkNickname(user.userId(), nickname);
    }

    /** AUTH-04, MY-03 */
    @PatchMapping("/me")
    public MeResponse updateProfile(@AuthenticationPrincipal AuthUser user, @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(user.userId(), request);
    }
}
