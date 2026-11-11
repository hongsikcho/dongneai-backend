/**
 * 후기 컨텍스트: 장소 후기(방문 월, 아이 연령대, 별점, 이용 조건 체크, 본문).
 * 한 회원은 같은 장소에 방문 월마다 후기 1개. 작성·수정·삭제는 place.api.PlaceApi로 장소의 후기 수·별점 합계에 반영한다.
 * 이용 조건 체크는 회원별 최신 후기 기준으로 집계해 PlaceApi.applyAmenityVotes로 장소 편의시설에 반영한다
 * (있음·없음 중 많은 쪽, 동률·체크 없음이면 운영자 입력값). 장소 목록의 편의시설 필터가 이 결과를 쓴다.
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO)
 * → infrastructure(QueryDSL) / presentation(컨트롤러).
 */
package com.dongneai.review;
