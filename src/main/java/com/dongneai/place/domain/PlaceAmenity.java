package com.dongneai.place.domain;

import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
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

/**
 * 장소 편의시설 집계. 후기 체크 결과를 모아 갱신하고, 시딩 단계에서는 운영자가 직접 입력한다.
 * 행이 없는 편의시설은 UNKNOWN으로 본다.
 */
@Getter
@Entity
@Table(name = "place_amenities")
@IdClass(PlaceAmenityId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceAmenity {

    @Id
    @Column(name = "place_id")
    private Long placeId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Amenity amenity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AmenityStatus status;

    @Column(name = "confirmed_count", nullable = false)
    private int confirmedCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlaceAmenity(Long placeId, Amenity amenity, AmenityStatus status, int confirmedCount, LocalDateTime now) {
        this.placeId = placeId;
        this.amenity = amenity;
        this.status = status;
        this.confirmedCount = confirmedCount;
        this.updatedAt = now;
    }

    /** 운영자 입력. 후기 집계 수(confirmedCount)는 건드리지 않는다. */
    public void setByAdmin(AmenityStatus status, LocalDateTime now) {
        this.status = status;
        this.updatedAt = now;
    }
}
