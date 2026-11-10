package com.dongneai.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.member.domain.NicknamePolicy;
import com.dongneai.member.domain.NicknamePolicy.Result;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NicknamePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "도토리맘, OK",
            "mom2026, OK",
            "가, INVALID_FORMAT",
            "열한글자닉네임입니다요, INVALID_FORMAT",
            "공백 포함, INVALID_FORMAT",
            "특수!문자, INVALID_FORMAT",
            "동네아이맘, FORBIDDEN_WORD",
            "Admin12, FORBIDDEN_WORD"
    })
    void 닉네임_규칙(String nickname, Result expected) {
        assertThat(NicknamePolicy.check(nickname)).isEqualTo(expected);
    }
}
