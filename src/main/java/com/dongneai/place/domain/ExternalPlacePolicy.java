package com.dongneai.place.domain;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 외부(카카오) 장소를 동네아이에 올릴 수 있는지와 노출 순서를 정하는 규칙.
 * 카카오 업종 이름(category_name, 예: "음식점 > 술집 > 호프,요리주점")을 기준으로 본다.
 */
public final class ExternalPlacePolicy {

    /** 아이와 함께 가기 어려운 업종. 검색 결과에서 빼고 등록도 막는다. */
    private static final List<String> DENIED = List.of(
            "술집", "주점", "호프", "유흥", "나이트", "단란", "룸살롱", "노래방", "노래연습장",
            "pc방", "당구장", "모텔", "여관", "여인숙", "성인", "카지노", "경마", "경륜", "복권", "담배", "게임장",
            "와인바", "칵테일", "포장마차", "이자카야", "바(bar)");

    /**
     * 장소 이름이나 검색어에 이 말이 들어가면 술 파는 곳으로 본다.
     * 카카오 업종이 "음식점 > 치킨", "음식점 > 양식"처럼 술집으로 안 잡히는 가게(포차, 펍, 호프 겸 치킨집)를 막는다.
     * "술" 한 글자는 미술·마술학원까지 걸려서 넣지 않는다.
     */
    private static final List<String> DENIED_NAME = List.of(
            "술집", "주점", "포차", "포장마차", "이자카야", "호프", "펍", "와인바", "칵테일", "맥주",
            "하이볼", "위스키", "노래방", "노래연습장", "pc방", "모텔", "룸살롱", "단란", "나이트");

    /** 아이와 가기 좋은 곳 신호. 업종 이름이나 장소 이름에 있으면 검색 결과 위로 올린다. */
    private static final List<String> KID_FRIENDLY = List.of(
            "키즈", "어린이", "유아", "아동", "소아", "놀이", "장난감", "유치원", "어린이집",
            "문화센터", "공원", "도서관", "박물관", "체험", "수유");

    private ExternalPlacePolicy() {
    }

    /** 업종 이름이나 장소 이름으로 본 부적합 장소. */
    public static boolean isDenied(String kakaoCategoryName, String placeName) {
        return containsAny(kakaoCategoryName, DENIED) || containsAny(placeName, DENIED_NAME);
    }

    /** "술집", "포차"처럼 부적합 장소를 찾는 검색어. 검색 자체를 하지 않는다. */
    public static boolean isDeniedQuery(String query) {
        return containsAny(query, DENIED_NAME);
    }

    public static boolean isKidFriendly(String kakaoCategoryName, String placeName) {
        return containsAny(kakaoCategoryName, KID_FRIENDLY) || containsAny(placeName, KID_FRIENDLY);
    }

    /**
     * 우리 카테고리로 검색할 때 카카오 업종 그룹 코드 (확실히 맞는 것만).
     * 소아과 → 병원(HP8), 유아 동반 식당 → 음식점(FD6). 나머지는 업종 분류가 제각각이라 걸지 않는다.
     */
    public static Optional<String> kakaoGroupCode(PlaceCategory category) {
        if (category == null) {
            return Optional.empty();
        }
        return switch (category) {
            case PEDIATRICS -> Optional.of("HP8");
            case RESTAURANT -> Optional.of("FD6");
            default -> Optional.empty();
        };
    }

    private static boolean containsAny(String text, List<String> words) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return words.stream().anyMatch(lower::contains);
    }
}
