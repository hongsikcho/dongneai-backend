package com.dongneai.member.presentation;

import com.dongneai.member.application.TermsService;
import com.dongneai.member.application.dto.TermsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    /** AUTH-03: 약관 목록 (비로그인 허용) */
    @GetMapping("/api/v1/terms")
    public List<TermsResponse> terms() {
        return termsService.currentTerms();
    }
}
