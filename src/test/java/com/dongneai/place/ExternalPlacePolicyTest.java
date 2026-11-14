package com.dongneai.place;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.place.domain.ExternalPlacePolicy;
import com.dongneai.place.domain.PlaceCategory;
import org.junit.jupiter.api.Test;

class ExternalPlacePolicyTest {

    @Test
    void 술집_숙박_유흥_업종은_막는다() {
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 술집 > 호프,요리주점")).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("숙박 > 모텔")).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("가정,생활 > PC방")).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 한식 > 국밥")).isFalse();
        assertThat(ExternalPlacePolicy.isDenied(null)).isFalse();
    }

    @Test
    void 업종이나_이름에_아이_관련_말이_있으면_아이와_가기_좋은_곳() {
        assertThat(ExternalPlacePolicy.isKidFriendly("음식점 > 카페 > 테마카페 > 키즈카페", "꿈나무")).isTrue();
        assertThat(ExternalPlacePolicy.isKidFriendly("의료,건강 > 병원 > 소아청소년과", "행복의원")).isTrue();
        assertThat(ExternalPlacePolicy.isKidFriendly("음식점 > 한식", "어린이 돈가스")).isTrue();
        assertThat(ExternalPlacePolicy.isKidFriendly("음식점 > 한식 > 국밥", "할매국밥")).isFalse();
    }

    @Test
    void 확실한_카테고리만_카카오_업종_그룹으로_좁힌다() {
        assertThat(ExternalPlacePolicy.kakaoGroupCode(PlaceCategory.PEDIATRICS)).contains("HP8");
        assertThat(ExternalPlacePolicy.kakaoGroupCode(PlaceCategory.RESTAURANT)).contains("FD6");
        assertThat(ExternalPlacePolicy.kakaoGroupCode(PlaceCategory.KIDS_CAFE)).isEmpty();
        assertThat(ExternalPlacePolicy.kakaoGroupCode(null)).isEmpty();
    }
}
