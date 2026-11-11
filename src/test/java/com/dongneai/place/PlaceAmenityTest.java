package com.dongneai.place;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.place.domain.PlaceAmenity;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PlaceAmenityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 12, 0, 0);

    @Test
    void 후기_체크가_없으면_운영자_입력값() {
        PlaceAmenity row = PlaceAmenity.byAdmin(1L, Amenity.PARKING, AmenityStatus.YES, NOW);
        assertThat(row.getStatus()).isEqualTo(AmenityStatus.YES);
        assertThat(row.getConfirmedCount()).isZero();
    }

    @Test
    void 후기_체크는_많은_쪽으로_판정하고_운영자_값보다_우선한다() {
        PlaceAmenity row = PlaceAmenity.byAdmin(1L, Amenity.PARKING, AmenityStatus.YES, NOW);
        row.applyVotes(1, 2, NOW);
        assertThat(row.getStatus()).isEqualTo(AmenityStatus.NO);
        assertThat(row.getConfirmedCount()).isEqualTo(2);
    }

    @Test
    void 동률이면_운영자_값_없으면_확인_필요() {
        PlaceAmenity admin = PlaceAmenity.byAdmin(1L, Amenity.STROLLER, AmenityStatus.NO, NOW);
        admin.applyVotes(1, 1, NOW);
        assertThat(admin.getStatus()).isEqualTo(AmenityStatus.NO);
        assertThat(admin.getConfirmedCount()).isEqualTo(1);

        PlaceAmenity votesOnly = PlaceAmenity.byVotes(1L, Amenity.STROLLER, 2, 0, NOW);
        assertThat(votesOnly.getStatus()).isEqualTo(AmenityStatus.YES);
        votesOnly.applyVotes(0, 0, NOW);
        assertThat(votesOnly.getStatus()).isEqualTo(AmenityStatus.UNKNOWN);
        assertThat(votesOnly.getConfirmedCount()).isZero();
    }
}
