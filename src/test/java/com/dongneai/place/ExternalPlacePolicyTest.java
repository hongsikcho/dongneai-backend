package com.dongneai.place;

import static com.dongneai.place.domain.PlaceCategory.CULTURE_CENTER;
import static com.dongneai.place.domain.PlaceCategory.KIDS_CAFE;
import static com.dongneai.place.domain.PlaceCategory.NURSING_ROOM;
import static com.dongneai.place.domain.PlaceCategory.PEDIATRICS;
import static com.dongneai.place.domain.PlaceCategory.PLAYGROUND;
import static com.dongneai.place.domain.PlaceCategory.RESTAURANT;
import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.place.domain.ExternalPlacePolicy;
import org.junit.jupiter.api.Test;

class ExternalPlacePolicyTest {

    @Test
    void 카테고리마다_맞는_업종만_받는다() {
        assertThat(ExternalPlacePolicy.allows(KIDS_CAFE, "음식점 > 카페 > 테마카페 > 키즈카페")).isTrue();
        assertThat(ExternalPlacePolicy.allows(PEDIATRICS, "의료,건강 > 병원 > 소아청소년과")).isTrue();
        assertThat(ExternalPlacePolicy.allows(PLAYGROUND, "여행 > 공원 > 어린이공원")).isTrue();
        assertThat(ExternalPlacePolicy.allows(NURSING_ROOM, "가정,생활 > 백화점")).isTrue();
        assertThat(ExternalPlacePolicy.allows(CULTURE_CENTER, "문화,예술 > 문화시설 > 도서관")).isTrue();
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, "음식점 > 한식 > 국밥")).isTrue();
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, "음식점 > 카페")).isTrue();

        assertThat(ExternalPlacePolicy.allows(KIDS_CAFE, "음식점 > 한식 > 국밥")).isFalse();
        assertThat(ExternalPlacePolicy.allows(PEDIATRICS, "의료,건강 > 병원 > 내과")).isFalse();
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, "가정,생활 > 백화점")).isFalse();
    }

    @Test
    void 술집_숙박_유흥은_어느_카테고리로도_받지_않는다() {
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, "음식점 > 술집 > 호프,요리주점")).isFalse();
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, "음식점 > 술집 > 실내포장마차")).isFalse();
        assertThat(ExternalPlacePolicy.allows(KIDS_CAFE, "가정,생활 > 놀이시설 > 성인오락실")).isFalse();
        assertThat(ExternalPlacePolicy.allows(PLAYGROUND, "가정,생활 > 놀이시설 > PC방")).isFalse();
    }

    @Test
    void 업종을_모르거나_카테고리가_없으면_받지_않는다() {
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, null)).isFalse();
        assertThat(ExternalPlacePolicy.allows(RESTAURANT, " ")).isFalse();
        assertThat(ExternalPlacePolicy.allows(null, "음식점 > 한식")).isFalse();
    }

    @Test
    void 업종이나_이름에_아이_관련_말이_있으면_아이와_가기_좋은_곳() {
        assertThat(ExternalPlacePolicy.isKidFriendly("음식점 > 카페 > 테마카페 > 키즈카페", "꿈나무")).isTrue();
        assertThat(ExternalPlacePolicy.isKidFriendly("의료,건강 > 병원 > 소아청소년과", "행복의원")).isTrue();
        assertThat(ExternalPlacePolicy.isKidFriendly("음식점 > 한식", "어린이 돈가스")).isTrue();
        assertThat(ExternalPlacePolicy.isKidFriendly("음식점 > 한식 > 국밥", "할매국밥")).isFalse();
    }

    @Test
    void 소아과만_카카오_병원_그룹으로_좁힌다() {
        assertThat(ExternalPlacePolicy.kakaoGroupCode(PEDIATRICS)).contains("HP8");
        assertThat(ExternalPlacePolicy.kakaoGroupCode(RESTAURANT)).isEmpty();
        assertThat(ExternalPlacePolicy.kakaoGroupCode(KIDS_CAFE)).isEmpty();
        assertThat(ExternalPlacePolicy.kakaoGroupCode(null)).isEmpty();
    }
}
