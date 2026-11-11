package com.dongneai.feed.domain;

import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.exception.ApiException;

/** 글 작성·수정 값. 생성 시점에 길이 규칙을 검사한다. placeId·ageGroup은 선택. */
public record PostContent(PostCategory category, String title, String content, AgeGroup ageGroup, Long placeId) {

    public static final int TITLE_MIN = 2;
    public static final int TITLE_MAX = 50;
    public static final int CONTENT_MIN = 5;
    public static final int CONTENT_MAX = 2000;

    public PostContent {
        title = title == null ? "" : title.strip();
        content = content == null ? "" : content.strip();
        if (category == null
                || title.length() < TITLE_MIN || title.length() > TITLE_MAX
                || content.length() < CONTENT_MIN || content.length() > CONTENT_MAX) {
            throw new ApiException(FeedErrorCode.INVALID_POST);
        }
    }
}
