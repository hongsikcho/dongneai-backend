/**
 * 회원 컨텍스트: 소셜 로그인, 약관 동의, 프로필, 온보딩 단계.
 *
 * <p>구조: domain(애그리거트·리포지토리 인터페이스·에러 코드) → application(유스케이스·DTO·포트)
 * → infrastructure(JPA·QueryDSL·외부 API) / presentation(컨트롤러). 다른 모듈은 member.api 패키지만 참조한다.
 */
package com.dongneai.member;
