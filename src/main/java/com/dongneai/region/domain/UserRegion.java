package com.dongneai.region.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원 관심 지역 (최대 3개, 기본 지역 1개) */
@Getter
@Entity
@Table(name = "user_regions")
@IdClass(UserRegionId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegion {

    public static final int MAX_PER_USER = 3;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryRegion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserRegion(Long userId, Long regionId, boolean primaryRegion, LocalDateTime createdAt) {
        this.userId = userId;
        this.regionId = regionId;
        this.primaryRegion = primaryRegion;
        this.createdAt = createdAt;
    }
}
