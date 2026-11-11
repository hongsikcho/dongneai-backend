package com.dongneai.feed.application;

import com.dongneai.feed.application.dto.Author;
import com.dongneai.feed.application.dto.CommentRequest;
import com.dongneai.feed.application.dto.CommentResponse;
import com.dongneai.feed.domain.Comment;
import com.dongneai.feed.domain.CommentRepository;
import com.dongneai.feed.domain.FeedErrorCode;
import com.dongneai.feed.domain.PostRepository;
import com.dongneai.shared.exception.ApiException;
import com.dongneai.shared.exception.CommonErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FEED-02 댓글·답글. 글의 comment_count는 보이는 댓글 수다. */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final FeedAssembler assembler;

    /** 최상위 댓글 작성순, 답글은 각 댓글 아래 작성순. 답글이 있는 삭제 댓글은 자리만 남긴다. */
    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long viewerId, Long postId) {
        postService.getVisible(postId);
        List<Comment> all = commentRepository.findByPostIdOrderByIdAsc(postId);
        Map<Long, String> nicknames = assembler.nicknames(all.stream().map(Comment::getUserId).toList());

        Map<Long, List<CommentResponse>> repliesByParent = new LinkedHashMap<>();
        for (Comment c : all) {
            if (c.getParentId() != null && c.isVisible()) {
                repliesByParent.computeIfAbsent(c.getParentId(), k -> new ArrayList<>())
                        .add(toResponse(c, nicknames, viewerId, List.of()));
            }
        }
        List<CommentResponse> result = new ArrayList<>();
        for (Comment c : all) {
            if (c.getParentId() != null) {
                continue;
            }
            List<CommentResponse> replies = repliesByParent.getOrDefault(c.getId(), List.of());
            if (c.isVisible() || !replies.isEmpty()) {
                result.add(toResponse(c, nicknames, viewerId, replies));
            }
        }
        return result;
    }

    @Transactional
    public CommentResponse create(Long userId, Long postId, CommentRequest request) {
        postService.getVisible(postId);
        Comment comment;
        if (request.parentId() == null) {
            comment = Comment.write(postId, userId, request.content());
        } else {
            Comment parent = commentRepository.findById(request.parentId())
                    .filter(p -> p.getPostId().equals(postId) && p.isVisible())
                    .orElseThrow(() -> new ApiException(FeedErrorCode.COMMENT_NOT_FOUND));
            comment = parent.reply(userId, request.content());
        }
        commentRepository.saveAndFlush(comment);
        postRepository.addCommentCount(postId, 1);
        String nickname = assembler.nicknames(List.of(userId)).get(userId);
        return toResponse(comment, Map.of(userId, nickname == null ? "" : nickname), userId, List.of());
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(Comment::isVisible)
                .orElseThrow(() -> new ApiException(FeedErrorCode.COMMENT_NOT_FOUND));
        if (!comment.isWrittenBy(userId)) {
            throw new ApiException(CommonErrorCode.FORBIDDEN);
        }
        comment.delete();
        commentRepository.flush();
        postRepository.addCommentCount(comment.getPostId(), -1);
    }

    private static CommentResponse toResponse(Comment c, Map<Long, String> nicknames, Long viewerId,
                                              List<CommentResponse> replies) {
        boolean deleted = !c.isVisible();
        String nickname = nicknames.get(c.getUserId());
        return new CommentResponse(c.getId(), c.getParentId(),
                deleted ? null : new Author(c.getUserId(), nickname == null || nickname.isEmpty() ? null : nickname),
                deleted ? null : c.getContent(), deleted, !deleted && c.isWrittenBy(viewerId), c.getCreatedAt(), replies);
    }
}
