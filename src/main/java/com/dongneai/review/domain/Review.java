package com.dongneai.review.domain;

import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import com.dongneai.shared.entity.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 장소 후기 애그리거트. 이용 조건 체크는 애그리거트 안의 값 컬렉션이다. */
@Getter
@Entity
@Table(name = "place_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "visited_on", nullable = false, length = 7)
    private YearMonth visitedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "child_age_group", nullable = false, length = 20)
    private AgeGroup childAgeGroup;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status;

    @Column(name = "reaction_count", nullable = false)
    private int reactionCount;

    @ElementCollection
    @CollectionTable(name = "review_amenity_checks", joinColumns = @JoinColumn(name = "review_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "amenity", length = 20)
    @Enumerated(EnumType.STRING)
    @Column(name = "value", nullable = false, length = 10)
    private Map<Amenity, AmenityStatus> amenityChecks = new HashMap<>();

    private Review(Long placeId, Long userId, ReviewContent content) {
        this.placeId = placeId;
        this.userId = userId;
        this.status = ReviewStatus.VISIBLE;
        apply(content);
    }

    public static Review write(Long placeId, Long userId, ReviewContent content) {
        return new Review(placeId, userId, content);
    }

    /** 본문·별점·체크 등을 새 값으로 바꾼다 (수정, 또는 삭제했던 같은 달 후기를 다시 쓸 때). */
    public void rewrite(ReviewContent content) {
        apply(content);
        this.status = ReviewStatus.VISIBLE;
    }

    public void delete() {
        this.status = ReviewStatus.DELETED;
    }

    public boolean isVisible() {
        return status == ReviewStatus.VISIBLE;
    }

    public boolean isDeleted() {
        return status == ReviewStatus.DELETED;
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    private void apply(ReviewContent content) {
        this.visitedOn = content.visitedOn();
        this.childAgeGroup = content.childAgeGroup();
        this.rating = content.rating();
        this.content = content.content();
        this.amenityChecks.clear();
        this.amenityChecks.putAll(content.amenityChecks());
    }
}
