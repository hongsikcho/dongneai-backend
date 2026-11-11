/**
 * 장소 컨텍스트: 장소 목록·상세·등록, 편의시설 집계, 카카오 장소 검색, 관리자 시딩.
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO·포트)
 * → infrastructure(JPA·QueryDSL·외부 API) / presentation(컨트롤러). 다른 모듈은 place.api 패키지만 참조한다.
 */
package com.dongneai.place;
