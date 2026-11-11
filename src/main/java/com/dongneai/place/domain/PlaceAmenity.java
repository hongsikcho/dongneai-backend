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
 * 장소 편의시설. 후기 체크(사람별 최신 후기)의 있음·없음 중 많은 쪽으로 판정하고,
 * 동률이거나 체크가 없으면 운영자 입력값, 그것도 없으면 UNKNOWN.
 * confirmedCount는 판정 결과와 같은 쪽으로 체크한 사람 수.
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
    @Column(name = "admin_status", length = 10)
    private AmenityStatus adminStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AmenityStatus status;

    @Column(name = "yes_count", nullable = false)
    private int yesCount;

    @Column(name = "no_count", nullable = false)
    private int noCount;

    @Column(name = "confirmed_count", nullable = false)
    private int confirmedCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private PlaceAmenity(Long placeId, Amenity amenity, LocalDateTime now) {
        this.placeId = placeId;
        this.amenity = amenity;
        this.updatedAt = now;
        recalculate();
    }

    /** 운영자 입력으로 만든다. */
    public static PlaceAmenity byAdmin(Long placeId, Amenity amenity, AmenityStatus status, LocalDateTime now) {
        PlaceAmenity row = new PlaceAmenity(placeId, amenity, now);
        row.setByAdmin(status, now);
        return row;
    }

    /** 후기 체크 집계로 만든다 (운영자 입력 없음). */
    public static PlaceAmenity byVotes(Long placeId, Amenity amenity, int yes, int no, LocalDateTime now) {
        PlaceAmenity row = new PlaceAmenity(placeId, amenity, now);
        row.applyVotes(yes, no, now);
        return row;
    }

    public void setByAdmin(AmenityStatus status, LocalDateTime now) {
        this.adminStatus = status;
        this.updatedAt = now;
        recalculate();
    }

    public void applyVotes(int yes, int no, LocalDateTime now) {
        if (yes == yesCount && no == noCount) {
            return;
        }
        this.yesCount = Math.max(yes, 0);
        this.noCount = Math.max(no, 0);
        this.updatedAt = now;
        recalculate();
    }

    private void recalculate() {
        if (yesCount > noCount) {
            status = AmenityStatus.YES;
        } else if (noCount > yesCount) {
            status = AmenityStatus.NO;
        } else {
            status = adminStatus == null ? AmenityStatus.UNKNOWN : adminStatus;
        }
        confirmedCount = switch (status) {
            case YES -> yesCount;
            case NO -> noCount;
            case UNKNOWN -> 0;
        };
    }
}
