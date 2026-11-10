package com.dongneai.member.domain;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** 닉네임 규칙: 한글·영문·숫자 2~10자, 운영자 사칭 단어 금지. */
public final class NicknamePolicy {

    private static final Pattern FORMAT = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");
    private static final List<String> FORBIDDEN_WORDS = List.of("관리자", "운영자", "동네아이", "admin", "운영팀");

    private NicknamePolicy() {
    }

    public enum Result { OK, INVALID_FORMAT, FORBIDDEN_WORD }

    public static Result check(String nickname) {
        if (nickname == null || !FORMAT.matcher(nickname).matches()) {
            return Result.INVALID_FORMAT;
        }
        String lower = nickname.toLowerCase(Locale.ROOT);
        boolean forbidden = FORBIDDEN_WORDS.stream().anyMatch(lower::contains);
        return forbidden ? Result.FORBIDDEN_WORD : Result.OK;
    }
}
