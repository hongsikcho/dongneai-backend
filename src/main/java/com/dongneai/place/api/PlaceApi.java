package com.dongneai.place.api;

/** 다른 모듈(후기·피드)이 장소를 확인하거나 장소 집계를 갱신할 때 쓰는 공개 API. */
public interface PlaceApi {

    /** 공개 중인 장소인지 확인한다. 없거나 숨김·병합된 장소면 PLACE_NOT_FOUND. */
    void requireVisible(Long placeId);

    /**
     * 후기 작성·수정·삭제를 장소 집계(후기 수, 별점 합계)에 반영한다.
     * 예: 작성 (1, rating), 삭제 (-1, -rating), 별점 수정 (0, new - old).
     */
    void applyReviewChange(Long placeId, int countDelta, int ratingDelta);
}
