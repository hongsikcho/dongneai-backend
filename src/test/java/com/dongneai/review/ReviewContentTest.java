package com.dongneai.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dongneai.review.domain.ReviewContent;
import com.dongneai.shared.code.AgeGroup;
import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import com.dongneai.shared.exception.ApiException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReviewContentTest {

    private static final YearMonth OCT = YearMonth.of(2026, 10);

    @Test
    void 본문_앞뒤_공백을_지우고_10자_미만이면_거절한다() {
        assertThat(content(OCT, 3, "   열 글자 이상 되는 후기   ").content()).isEqualTo("열 글자 이상 되는 후기");
        assertThatThrownBy(() -> content(OCT, 3, "  짧아요       ")).isInstanceOf(ApiException.class);
    }

    @Test
    void 별점은_1에서_5까지만() {
        assertThatThrownBy(() -> content(OCT, 0, "별점이 0점인 후기입니다.")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> content(OCT, 6, "별점이 6점인 후기입니다.")).isInstanceOf(ApiException.class);
    }

    @Test
    void 비어_있는_체크는_버린다() {
        Map<Amenity, AmenityStatus> checks = new HashMap<>();
        checks.put(Amenity.PARKING, AmenityStatus.YES);
        checks.put(Amenity.STROLLER, null);
        ReviewContent c = new ReviewContent(OCT, AgeGroup.AGE_1_2, 4, "주차 가능한 곳이었어요.", checks);
        assertThat(c.amenityChecks()).containsOnlyKeys(Amenity.PARKING);
    }

    @Test
    void 이번_달_이후는_방문_월로_쓸_수_없다() {
        content(OCT, 3, "이번 달 방문 후기입니다.").requireNotFuture(OCT);
        assertThatThrownBy(() -> content(OCT.plusMonths(1), 3, "다음 달 방문 후기입니다.").requireNotFuture(OCT))
                .isInstanceOf(ApiException.class);
    }

    private ReviewContent content(YearMonth month, int rating, String text) {
        return new ReviewContent(month, AgeGroup.AGE_1_2, rating, text, null);
    }
}
