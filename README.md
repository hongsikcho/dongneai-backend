# dongneai-backend

동네아이 백엔드. Spring Boot 3.5 · Java 17 · MySQL 8 · Flyway · QueryDSL · JWT.
기획서·화면 정의서·API 명세서·아키텍처는 Claude Docs "동네아이 서비스 기획서 v2"에 있다.

## 로컬 실행

필요: JDK 17+, MySQL 8 (Homebrew `mysql@8.4` 또는 `docker compose up -d`)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
curl localhost:8080/actuator/health
```

- 기동 시 Flyway가 `db/migration`(스키마)과 `db/seed`(로컬 가상 데이터)를 적용한다.
- IntelliJ: 실행 구성 Active profiles = `local`

### 환경변수

비밀값은 코드에 넣지 않고 환경변수로만 주입한다 (`.env.example` 참고).

| 이름 | 용도 | 로컬 |
| --- | --- | --- |
| `KAKAO_REST_API_KEY` | 장소 등록용 카카오 장소 검색 | IntelliJ 실행 구성 Environment variables |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | 운영 DB | local 프로필은 불필요 |
| `JWT_SECRET` | 토큰 서명 (32바이트 이상) | local 프로필은 불필요 |

### 개발용 토큰 (local 프로필 전용)

```bash
TOKEN=$(curl -s -X POST 'localhost:8080/api/v1/dev/token?devId=me' | jq -r .accessToken)
ADMIN=$(curl -s -X POST 'localhost:8080/api/v1/dev/token?devId=admin&admin=true' | jq -r .accessToken)
curl -s localhost:8080/api/v1/auth/me -H "Authorization: Bearer $TOKEN"
```

## 테스트

```bash
./gradlew test               # 단위 테스트 (DB 불필요)
./gradlew integrationTest    # 통합 테스트 (로컬 MySQL 필요)
```

## 패키지 구조

DDD 모듈형 모놀리스. 배포 단위는 하나이고, 최상위 패키지 하나가 바운디드 컨텍스트 하나다.
경계는 Spring Modulith가 `ModularityTest`(`./gradlew test`에 포함)로 검증한다.

| 모듈 | 내용 |
| --- | --- |
| `member` | 회원, 인증, 약관, 프로필, 온보딩 단계, 로컬 개발용 토큰 발급 |
| `region` | 지역 검색, 관심 지역 |
| `place` | 장소 목록·상세·등록, 편의시설 집계, 카카오 장소 검색, 관리자 시딩 |
| `review` | 장소 후기 작성·수정·삭제·목록, 별점 (장소 후기 수·별점 합계는 `PlaceApi`로 갱신) |
| `feed`, `moderation`, `media`, `admin` | 예정 |
| `shared` | 공통 커널 (BaseTimeEntity, ApiException·ErrorCode, CursorPage, AgeGroup, 코드표) |
| `security`, `config` | JWT 인증, JPA·QueryDSL 설정 |

모듈 안의 구조:

```
<module>/
├─ api/             다른 모듈에 공개하는 인터페이스·레코드 (@NamedInterface)
├─ domain/          애그리거트, 값 객체, 리포지토리 인터페이스, 모듈 에러 코드
├─ application/     유스케이스 서비스, dto/(요청·응답), port/(외부 시스템 인터페이스)
├─ infrastructure/  QueryDSL 조회, 외부 API 클라이언트 (port 구현)
└─ presentation/    REST 컨트롤러
```

규칙

- 다른 모듈은 `<module>.api`와 `shared`, `security`만 참조한다. 다른 모듈의 `domain`·`application` 등을 import하면 `ModularityTest`가 실패한다.
- 모듈 간 참조는 ID로만 한다. 다른 모듈 엔티티와 JPA 연관관계를 맺거나 다른 모듈 테이블을 QueryDSL로 조인하지 않는다.
- 상태 변경이 다른 모듈로 퍼지면 도메인 이벤트로 알린다 (예: 후기 작성 → 장소 편의시설 집계). 지금은 같은 트랜잭션에서 동기 처리한다.
- 에러 코드는 모듈별 enum(`MemberErrorCode`, `PlaceErrorCode` …)이 `shared.exception.ErrorCode`를 구현한다. 응답의 `code` 값은 기존과 같다.
- 앱 코드표(`/api/v1/meta/codes`)는 각 모듈이 `CodeTable` 빈으로 자기 코드를 제공한다.
