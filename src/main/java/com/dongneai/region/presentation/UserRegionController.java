package com.dongneai.region.presentation;

import com.dongneai.region.application.UserRegionService;
import com.dongneai.region.application.dto.UserRegionResponse;
import com.dongneai.region.application.dto.UserRegionsRequest;
import com.dongneai.security.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/regions")
@RequiredArgsConstructor
public class UserRegionController {

    private final UserRegionService userRegionService;

    @GetMapping
    public List<UserRegionResponse> myRegions(@AuthenticationPrincipal AuthUser user) {
        return userRegionService.myRegions(user.userId());
    }

    @PutMapping
    public List<UserRegionResponse> replace(@AuthenticationPrincipal AuthUser user,
                                            @RequestBody @Valid UserRegionsRequest request) {
        return userRegionService.replace(user.userId(), request);
    }
}
