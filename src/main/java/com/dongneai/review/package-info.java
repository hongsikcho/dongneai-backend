/**
 * 후기 컨텍스트: 장소 후기(방문 월, 아이 연령대, 별점, 이용 조건 체크, 본문).
 * 한 회원은 같은 장소에 방문 월마다 후기 1개. 작성·수정·삭제는 place.api.PlaceApi로 장소의 후기 수·별점 합계에 반영한다.
 * 이용 조건 체크는 후기에만 보여 주고 장소의 편의시설 표시에는 집계하지 않는다 (장소 표시는 관리자 입력값).
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO)
 * → infrastructure(QueryDSL) / presentation(컨트롤러).
 */
package com.dongneai.review;
