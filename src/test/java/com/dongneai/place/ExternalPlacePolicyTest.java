package com.dongneai.place;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.place.domain.ExternalPlacePolicy;
import com.dongneai.place.domain.PlaceCategory;
import org.junit.jupiter.api.Test;

class ExternalPlacePolicyTest {

    @Test
    void 술집_숙박_유흥_업종은_막는다() {
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 술집 > 호프,요리주점", null)).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("숙박 > 모텔", null)).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("가정,생활 > PC방", null)).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 한식 > 국밥", null)).isFalse();
        assertThat(ExternalPlacePolicy.isDenied(null, null)).isFalse();
    }

    @Test
    void 업종이_술집이_아니어도_이름이_포차_펍이면_막는다() {
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 치킨", "안양 치킨호프")).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 양식", "범계 수제맥주 펍")).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 한식", "평촌 실내포차")).isTrue();
        assertThat(ExternalPlacePolicy.isDenied("음식점 > 치킨", "교촌치킨 평촌점")).isFalse();
    }

    @Test
    void 술집을_찾는_검색어는_검색하지_않는다() {
        assertThat(ExternalPlacePolicy.isDeniedQuery("안양 술집")).isTrue();
        assertThat(ExternalPlacePolicy.isDeniedQuery("포차")).isTrue();
        assertThat(ExternalPlacePolicy.isDeniedQuery("미술학원")).isFalse();
        assertThat(ExternalPlacePolicy.isDeniedQuery("키즈카페")).isFalse();
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
