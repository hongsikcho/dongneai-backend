/**
 * 후기 컨텍스트 (5단계): 장소 후기, 편의시설 체크. 작성·수정·삭제 시 이벤트로 place 집계를 갱신한다.
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO·포트)
 * → infrastructure(JPA·QueryDSL·외부 API) / presentation(컨트롤러). 다른 모듈은 review.api 패키지만 참조한다.
 */
package com.dongneai.review;
