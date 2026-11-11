package com.dongneai.feed.application;

import com.dongneai.feed.domain.PostCategory;
import com.dongneai.shared.code.CodeTable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 피드 글 카테고리 코드표 (/api/v1/meta/codes 의 postCategories). */
@Order(20)
@Component
class FeedCodeTable implements CodeTable {

    @Override
    public Map<String, List<Code>> codes() {
        return Map.of("postCategories",
                Arrays.stream(PostCategory.values()).map(c -> new Code(c.name(), c.getLabel())).toList());
    }
}
