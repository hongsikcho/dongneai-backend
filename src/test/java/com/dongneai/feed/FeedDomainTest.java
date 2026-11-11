package com.dongneai.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dongneai.feed.domain.Comment;
import com.dongneai.feed.domain.FeedTimeline;
import com.dongneai.feed.domain.PostCategory;
import com.dongneai.feed.domain.PostContent;
import com.dongneai.shared.exception.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FeedDomainTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 11, 12, 10, 0);

    @Test
    void 글과_후기를_최신순으로_섞고_마지막_항목_시각을_커서로_준다() {
        List<FeedTimeline.Entry<String>> entries = List.of(
                new FeedTimeline.Entry<>(T.minusHours(3), "post-3h"),
                new FeedTimeline.Entry<>(T.minusHours(1), "review-1h"),
                new FeedTimeline.Entry<>(T.minusHours(2), "post-2h"));

        FeedTimeline.Page<String> page = FeedTimeline.merge(entries, 2);

        assertThat(page.items()).containsExactly("review-1h", "post-2h");
        assertThat(FeedTimeline.parseCursor(page.nextCursor())).isEqualTo(T.minusHours(2));
        assertThat(FeedTimeline.merge(entries, 3).nextCursor()).isNull();
    }

    @Test
    void 제목과_본문_길이를_검사한다() {
        assertThat(new PostContent(PostCategory.QUESTION, "  질문있어요  ", "본문 다섯자 이상", null, null).title())
                .isEqualTo("질문있어요");
        assertThatThrownBy(() -> new PostContent(PostCategory.QUESTION, "질", "본문 다섯자 이상", null, null))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> new PostContent(null, "제목입니다", "본문 다섯자 이상", null, null))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 답글에는_답글을_달_수_없다() {
        Comment parent = Comment.write(1L, 1L, "첫 댓글");
        ReflectionTestUtils.setField(parent, "id", 10L);
        Comment reply = parent.reply(2L, "답글");
        assertThat(reply.getParentId()).isEqualTo(10L);

        assertThatThrownBy(() -> reply.reply(3L, "답글의 답글")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> Comment.write(1L, 1L, "   ")).isInstanceOf(ApiException.class);
    }
}
