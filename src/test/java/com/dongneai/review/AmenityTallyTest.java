package com.dongneai.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.review.domain.AmenityTally;
import com.dongneai.review.domain.Review;
import com.dongneai.review.domain.ReviewContent;
import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AmenityTallyTest {

    @Test
    void 회원마다_가장_최근_방문_후기만_센다() {
        // 회원 1: 9월엔 주차 있음, 10월엔 주차 없음 → 10월 것만 센다
        Review oldOne = review(1L, 1L, YearMonth.of(2026, 9), Map.of(Amenity.PARKING, AmenityStatus.YES));
        Review newOne = review(2L, 1L, YearMonth.of(2026, 10), Map.of(Amenity.PARKING, AmenityStatus.NO));
        // 회원 2: 주차 있음, 유모차 모름(세지 않음)
        Review other = review(3L, 2L, YearMonth.of(2026, 8),
                Map.of(Amenity.PARKING, AmenityStatus.YES, Amenity.STROLLER, AmenityStatus.UNKNOWN));

        Map<Amenity, AmenityTally.Count> tally = AmenityTally.of(List.of(oldOne, newOne, other));

        assertThat(tally.get(Amenity.PARKING)).isEqualTo(new AmenityTally.Count(1, 1));
        assertThat(tally.get(Amenity.STROLLER)).isEqualTo(new AmenityTally.Count(0, 0));
    }

    @Test
    void 최신_후기에_체크가_없으면_그_회원은_세지_않는다() {
        Review checked = review(1L, 1L, YearMonth.of(2026, 9), Map.of(Amenity.PARKING, AmenityStatus.YES));
        Review latestWithoutChecks = review(2L, 1L, YearMonth.of(2026, 10), Map.of());

        assertThat(AmenityTally.of(List.of(checked, latestWithoutChecks))).isEmpty();
    }

    private Review review(Long id, Long userId, YearMonth month, Map<Amenity, AmenityStatus> checks) {
        Review review = Review.write(1L, userId,
                new ReviewContent(month, AgeGroup.AGE_1_2, 4, "테스트용 후기 본문입니다.", checks));
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }
}
