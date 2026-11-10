package com.dongneai.place.domain;

import com.dongneai.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceCategory category;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "kakao_place_id", length = 30)
    private String kakaoPlaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PlaceSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceStatus status;

    @Column(name = "merged_into_id")
    private Long mergedIntoId;

    @Column(name = "created_by")
    private Long createdBy;

    /** 운영자가 정보를 마지막으로 확인한 날. 사용자 등록 장소는 확인 전까지 null. */
    @Column(name = "verified_at")
    private LocalDate verifiedAt;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "post_count", nullable = false)
    private int postCount;

    @Builder
    private Place(Long regionId, String name, PlaceCategory category, String address, BigDecimal lat,
                  BigDecimal lng, String kakaoPlaceId, PlaceSource source, Long createdBy, LocalDate verifiedAt) {
        this.regionId = regionId;
        this.name = name;
        this.category = category;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.kakaoPlaceId = kakaoPlaceId;
        this.source = source;
        this.status = PlaceStatus.VISIBLE;
        this.createdBy = createdBy;
        this.verifiedAt = verifiedAt;
    }

    public void update(Long regionId, String name, PlaceCategory category, String address, BigDecimal lat, BigDecimal lng) {
        this.regionId = regionId;
        this.name = name;
        this.category = category;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public void changeStatus(PlaceStatus status) {
        this.status = status;
    }

    public void markVerified(LocalDate date) {
        this.verifiedAt = date;
    }

    public boolean isVisible() {
        return status == PlaceStatus.VISIBLE;
    }
}
