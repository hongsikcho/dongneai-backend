package com.dongneai.feed.domain;

import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.entity.BaseTimeEntity;
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

/** 동네 글. 공감 수·댓글 수는 리포지토리의 원자적 증감 쿼리로만 바꾼다. */
@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category;

    @Column(name = "place_id")
    private Long placeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 20)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "reaction_count", nullable = false)
    private int reactionCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    private Post(Long userId, Long regionId, PostContent content) {
        this.userId = userId;
        this.regionId = regionId;
        this.status = PostStatus.VISIBLE;
        apply(content);
    }

    public static Post write(Long userId, Long regionId, PostContent content) {
        return new Post(userId, regionId, content);
    }

    public void edit(PostContent content) {
        apply(content);
    }

    public void delete() {
        this.status = PostStatus.DELETED;
    }

    public boolean isVisible() {
        return status == PostStatus.VISIBLE;
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    private void apply(PostContent content) {
        this.category = content.category();
        this.title = content.title();
        this.content = content.content();
        this.ageGroup = content.ageGroup();
        this.placeId = content.placeId();
    }
}
