package com.dongneai.place.api;

import com.dongneai.shared.code.Amenity;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 다른 모듈(후기·피드)이 장소를 확인하거나 장소 집계를 갱신할 때 쓰는 공개 API. */
public interface PlaceApi {

    /** 공개 중인 장소인지 확인한다. 없거나 숨김·병합된 장소면 PLACE_NOT_FOUND. */
    void requireVisible(Long placeId);

    /**
     * 후기 작성·수정·삭제를 장소 집계(후기 수, 별점 합계)에 반영한다.
     * 예: 작성 (1, rating), 삭제 (-1, -rating), 별점 수정 (0, new - old).
     */
    void applyReviewChange(Long placeId, int countDelta, int ratingDelta);

    /**
     * 후기 체크 집계(사람별 최신 후기 기준 있음·없음 수)로 장소 편의시설을 다시 판정한다.
     * 맵에 없는 편의시설은 0표로 본다.
     */
    void applyAmenityVotes(Long placeId, Map<Amenity, AmenityVote> votes);

    /** 피드 글 장소 태그 추가·해제를 장소의 '이 장소 이야기' 수(post_count)에 반영한다. */
    void applyPostChange(Long placeId, int countDelta);

    /** 표시용 장소 정보 일괄 조회 (숨김·병합 장소 포함). 없는 ID는 결과에서 빠진다. */
    Map<Long, PlaceRef> findPlaces(Collection<Long> placeIds);

    /** 지역의 공개 장소 ID 목록 (피드의 새 후기 카드용). */
    List<Long> findVisiblePlaceIds(Long regionId);

    /** category는 PlaceCategory 코드 (예: KIDS_CAFE). 라벨은 코드표 API로 표시한다. */
    record PlaceRef(Long id, String name, String category) {
    }

    record AmenityVote(int yes, int no) {
        public static final AmenityVote NONE = new AmenityVote(0, 0);
    }
}
