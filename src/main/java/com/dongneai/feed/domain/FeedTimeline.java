package com.dongneai.feed.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 홈 피드 타임라인: 글과 새 후기 카드를 작성 시각 최신순으로 섞는다.
 * 커서는 마지막 항목의 작성 시각(ISO 문자열)이며, 다음 페이지는 그보다 이전 항목만 가져온다.
 */
public final class FeedTimeline {

    public record Entry<T>(LocalDateTime createdAt, T item) {
    }

    public record Page<T>(List<T> items, String nextCursor) {
    }

    private FeedTimeline() {
    }

    /** 각 출처에서 size + 1건씩 가져온 목록을 합쳐 size건과 다음 커서를 만든다. */
    public static <T> Page<T> merge(List<Entry<T>> entries, int size) {
        List<Entry<T>> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing((Function<Entry<T>, LocalDateTime>) Entry::createdAt).reversed());
        if (sorted.size() <= size) {
            return new Page<>(sorted.stream().map(Entry::item).toList(), null);
        }
        List<Entry<T>> page = sorted.subList(0, size);
        return new Page<>(page.stream().map(Entry::item).toList(), page.get(size - 1).createdAt().toString());
    }

    public static LocalDateTime parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(cursor);
    }
}
