package com.dongneai.member.application.dto;

import com.dongneai.member.domain.TermsCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AgreementRequest(@NotEmpty List<@Valid Item> agreements) {

    public record Item(@NotNull TermsCode code, boolean agreed) {
    }
}
