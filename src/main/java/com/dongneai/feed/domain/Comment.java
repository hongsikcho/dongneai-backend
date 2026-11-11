package com.dongneai.feed.domain;

import com.dongneai.shared.entity.BaseTimeEntity;
import com.dongneai.shared.exception.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 댓글. parentId가 있으면 답글이며, 답글은 1단계까지만 달 수 있다. */
@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    public static final int MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = MAX_LENGTH)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status;

    private Comment(Long postId, Long userId, Long parentId, String content) {
        String text = content == null ? "" : content.strip();
        if (text.isEmpty() || text.length() > MAX_LENGTH) {
            throw new ApiException(FeedErrorCode.INVALID_COMMENT);
        }
        this.postId = postId;
        this.userId = userId;
        this.parentId = parentId;
        this.content = text;
        this.status = CommentStatus.VISIBLE;
    }

    public static Comment write(Long postId, Long userId, String content) {
        return new Comment(postId, userId, null, content);
    }

    /** 이 댓글에 답글을 단다. 답글에는 답글을 달 수 없다. */
    public Comment reply(Long userId, String content) {
        if (parentId != null) {
            throw new ApiException(FeedErrorCode.INVALID_PARENT_COMMENT);
        }
        return new Comment(postId, userId, id, content);
    }

    public void delete() {
        this.status = CommentStatus.DELETED;
    }

    public boolean isVisible() {
        return status == CommentStatus.VISIBLE;
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }
}
