package com.dongneai.common.response;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 페이징 응답. size + 1건을 조회해 다음 페이지 존재 여부를 판단한다.
 */
public record CursorPage<T>(List<T> items, String nextCursor) {

    public static <T> CursorPage<T> of(List<T> fetched, int size, Function<T, String> cursorOf) {
        if (fetched.size() <= size) {
            return new CursorPage<>(fetched, null);
        }
        List<T> page = fetched.subList(0, size);
        return new CursorPage<>(page, cursorOf.apply(page.get(size - 1)));
    }
}
