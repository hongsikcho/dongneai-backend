/**
 * 관리자 웹용 API 조합 (예정). 도메인 규칙은 각 컨텍스트에 두고 여기서는 조합만 한다.
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO·포트)
 * → infrastructure(JPA·QueryDSL·외부 API) / presentation(컨트롤러). 다른 모듈은 admin.api 패키지만 참조한다.
 */
package com.dongneai.admin;
