package com.dongneai.member.application.dto;

/** reason: available=false일 때 INVALID_FORMAT, FORBIDDEN_WORD, DUPLICATED 중 하나 */
public record NicknameCheckResponse(boolean available, String reason) {

    public static NicknameCheckResponse ok() {
        return new NicknameCheckResponse(true, null);
    }

    public static NicknameCheckResponse unavailable(String reason) {
        return new NicknameCheckResponse(false, reason);
    }
}
