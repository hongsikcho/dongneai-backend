package com.dongneai.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공감. 지금은 글 공감(POST, LIKE)만 쓴다. */
@Getter
@Entity
@Table(name = "reactions")
@IdClass(ReactionId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reaction {

    public static final String LIKE = "LIKE";

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20)
    private ReactionTarget targetType;

    @Id
    @Column(name = "target_id")
    private Long targetId;

    @Id
    @Column(length = 10)
    private String type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Reaction(Long userId, ReactionTarget targetType, Long targetId, String type, LocalDateTime now) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.type = type;
        this.createdAt = now;
    }

    public static Reaction likePost(Long userId, Long postId, LocalDateTime now) {
        return new Reaction(userId, ReactionTarget.POST, postId, LIKE, now);
    }

    public static ReactionId postLikeId(Long userId, Long postId) {
        return new ReactionId(userId, ReactionTarget.POST, postId, LIKE);
    }
}
