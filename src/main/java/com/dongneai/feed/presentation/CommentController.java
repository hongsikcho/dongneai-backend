package com.dongneai.feed.presentation;

import com.dongneai.feed.application.CommentService;
import com.dongneai.feed.application.dto.CommentRequest;
import com.dongneai.feed.application.dto.CommentResponse;
import com.dongneai.security.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** FEED-02 댓글 목록 (답글 포함) */
    @GetMapping("/posts/{postId:\\d+}/comments")
    public List<CommentResponse> list(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId) {
        return commentService.list(user.userId(), postId);
    }

    /** 댓글·답글 작성. parentId가 있으면 답글 (1단계까지) */
    @PostMapping("/posts/{postId:\\d+}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@AuthenticationPrincipal AuthUser user, @PathVariable Long postId,
                                  @RequestBody @Valid CommentRequest request) {
        return commentService.create(user.userId(), postId, request);
    }

    @DeleteMapping("/comments/{commentId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long commentId) {
        commentService.delete(user.userId(), commentId);
    }
}
