/**
 * 피드 컨텍스트: 동네 글(질문·육아 팁·행사·체험·자유), 댓글·답글, 공감, 장소 태그.
 * 홈 피드는 글과 그 동네 장소의 새 후기(review.api)를 시간순으로 섞어 보여 준다.
 * 장소 태그 수는 place.api.PlaceApi.applyPostChange로 장소의 '이 장소 이야기' 수에 반영한다.
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO)
 * → infrastructure(QueryDSL) / presentation(컨트롤러).
 */
package com.dongneai.feed;
