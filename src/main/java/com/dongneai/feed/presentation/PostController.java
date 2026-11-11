package com.dongneai.feed.presentation;

import com.dongneai.feed.application.PostService;
import com.dongneai.feed.application.dto.LikeResponse;
import com.dongneai.feed.application.dto.PostDetailResponse;
import com.dongneai.feed.application.dto.PostRequest;
import com.dongneai.feed.application.dto.PostSummaryResponse;
import com.dongneai.security.AuthUser;
import com.dongneai.shared.response.CursorPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** FEED-03 글 작성 (내 관심 동네에만) */
    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse create(@AuthenticationPrincipal AuthUser user, @RequestBody @Valid PostRequest request) {
        return postService.create(user.userId(), request);
    }

    /** FEED-02 글 상세 */
    @GetMapping("/posts/{postId:\\d+}")
    public PostDetailResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId) {
        return postService.get(user.userId(), postId);
    }

    /** FEED-03 글 수정 (작성자만). 동네는 바꿀 수 없다. */
    @PatchMapping("/posts/{postId:\\d+}")
    public PostDetailResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId,
                                     @RequestBody @Valid PostRequest request) {
        return postService.update(user.userId(), postId, request);
    }

    @DeleteMapping("/posts/{postId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId) {
        postService.delete(user.userId(), postId);
    }

    /** 공감 (여러 번 호출해도 1번) */
    @PutMapping("/posts/{postId:\\d+}/like")
    public LikeResponse like(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId) {
        return postService.like(user.userId(), postId);
    }

    @DeleteMapping("/posts/{postId:\\d+}/like")
    public LikeResponse unlike(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId) {
        return postService.unlike(user.userId(), postId);
    }

    /** PLACE-02 '이 장소 이야기' 탭 */
    @GetMapping("/places/{placeId:\\d+}/posts")
    public CursorPage<PostSummaryResponse> listByPlace(@AuthenticationPrincipal AuthUser user,
                                                       @PathVariable Long placeId,
                                                       @RequestParam(required = false) String cursor,
                                                       @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return postService.listByPlace(user.userId(), placeId, cursor, size);
    }
}
