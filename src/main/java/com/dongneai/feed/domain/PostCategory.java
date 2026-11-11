package com.dongneai.feed.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 피드 글 카테고리 (현황판 기준, 출시 전 확정 필요). */
@Getter
@RequiredArgsConstructor
public enum PostCategory {
    QUESTION("질문"),
    TIP("육아 팁"),
    EVENT("행사·체험"),
    FREE("자유");

    private final String label;
}
