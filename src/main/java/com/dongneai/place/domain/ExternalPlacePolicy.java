package com.dongneai.place.domain;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 외부(카카오) 장소를 동네아이에 올릴 수 있는지와 노출 순서를 정하는 규칙.
 * 카카오 업종 이름(category_name, 예: "음식점 > 카페 > 테마카페 > 키즈카페")을 기준으로 본다.
 *
 * <p>나쁜 곳을 골라 막는 대신, 카테고리마다 받아줄 업종을 정해 두고 거기 맞는 곳만 통과시킨다 (허용 목록).
 * 장소 이름이나 검색어는 보지 않는다 — 이름 금지어는 끝이 없고 오탐이 많다.
 */
public final class ExternalPlacePolicy {

    /** 카테고리별로 받아줄 카카오 업종 이름 조각. 하나라도 들어 있으면 통과. */
    private static final Map<PlaceCategory, List<String>> ALLOWED = Map.of(
            PlaceCategory.KIDS_CAFE, List.of("키즈카페", "실내놀이터", "놀이시설", "키즈"),
            PlaceCategory.PEDIATRICS, List.of("소아"),
            PlaceCategory.PLAYGROUND, List.of("놀이터", "공원", "놀이시설"),
            PlaceCategory.NURSING_ROOM, List.of("수유", "백화점", "쇼핑", "마트", "아울렛",
                    "행정복지센터", "주민센터", "보건소", "도서관", "문화시설"),
            PlaceCategory.CULTURE_CENTER, List.of("문화센터", "문화시설", "도서관", "박물관", "미술관",
                    "과학관", "체험", "유아", "어린이"),
            // 식당·카페는 음식점 전체. 술집 갈래는 아래 DENIED가 뺀다.
            PlaceCategory.RESTAURANT, List.of("음식점"));

    /** 어느 카테고리로도 받지 않는 업종 (안전망). 카카오 분류에서 술집은 "음식점 > 술집" 한 갈래다. */
    private static final List<String> DENIED = List.of(
            "술집", "주점", "유흥", "성인", "숙박", "노래방", "노래연습장", "pc방", "당구장",
            "오락실", "게임장", "카지노", "경마", "경륜", "복권", "담배");

    /** 아이와 가기 좋은 곳 신호. 업종 이름이나 장소 이름에 있으면 검색 결과 위로 올린다. */
    private static final List<String> KID_FRIENDLY = List.of(
            "키즈", "어린이", "유아", "아동", "소아", "놀이", "장난감", "유치원", "어린이집",
            "문화센터", "공원", "도서관", "박물관", "체험", "수유");

    private ExternalPlacePolicy() {
    }

    /** 이 카카오 업종의 장소를 이 카테고리로 올릴 수 있는가. 업종을 모르면 받지 않는다. */
    public static boolean allows(PlaceCategory category, String kakaoCategoryName) {
        if (category == null || kakaoCategoryName == null || kakaoCategoryName.isBlank()) {
            return false;
        }
        return !containsAny(kakaoCategoryName, DENIED) && containsAny(kakaoCategoryName, ALLOWED.get(category));
    }

    public static boolean isKidFriendly(String kakaoCategoryName, String placeName) {
        return containsAny(kakaoCategoryName, KID_FRIENDLY) || containsAny(placeName, KID_FRIENDLY);
    }

    /**
     * 카카오 검색을 업종 그룹으로 좁힐 수 있는 카테고리 (확실히 맞는 것만). 소아과 → 병원(HP8).
     * 식당은 음식점(FD6)으로 좁히면 카페(CE7)가 빠져서 걸지 않는다.
     */
    public static Optional<String> kakaoGroupCode(PlaceCategory category) {
        return category == PlaceCategory.PEDIATRICS ? Optional.of("HP8") : Optional.empty();
    }

    private static boolean containsAny(String text, List<String> words) {
        if (text == null || text.isBlank() || words == null) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return words.stream().anyMatch(lower::contains);
    }
}
