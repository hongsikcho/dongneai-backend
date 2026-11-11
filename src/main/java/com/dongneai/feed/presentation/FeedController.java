package com.dongneai.feed.presentation;

import com.dongneai.feed.application.FeedService;
import com.dongneai.feed.application.dto.FeedItemResponse;
import com.dongneai.feed.domain.PostCategory;
import com.dongneai.security.AuthUser;
import com.dongneai.shared.response.CursorPage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /** FEED-01 홈 피드. category가 없으면 그 동네 장소의 새 후기 카드도 섞는다. cursor는 응답의 nextCursor. */
    @GetMapping
    public CursorPage<FeedItemResponse> feed(@AuthenticationPrincipal AuthUser user,
                                             @RequestParam Long regionId,
                                             @RequestParam(required = false) PostCategory category,
                                             @RequestParam(required = false) String cursor,
                                             @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return feedService.feed(user.userId(), regionId, category, cursor, size);
    }
}
