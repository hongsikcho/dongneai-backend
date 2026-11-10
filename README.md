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

| 패키지 | 내용 |
| --- | --- |
| `common` | 에러 코드·예외 처리, 커서 페이지, 코드표 API |
| `config` / `security` | JPA·QueryDSL 설정, JWT 인증 |
| `member` | 회원, 인증, 약관, 프로필 |
| `region` | 지역 검색, 관심 지역 |
| `place` | 장소 목록·상세·등록, 카카오 장소 검색, 관리자 시딩 |
| `review`, `feed`, `moderation`, `image`, `admin` | 예정 |
| `dev` | 로컬 전용 토큰 발급 |
