package com.dongneai.member.dto;

import com.dongneai.member.domain.TermsCode;

public record TermsResponse(TermsCode code, String title, boolean required, String version) {

    public static TermsResponse from(TermsCode terms) {
        return new TermsResponse(terms, terms.getTitle(), terms.isRequired(), TermsCode.CURRENT_VERSION);
    }
}
