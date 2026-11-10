-- 동네아이 MVP 초기 스키마 (MySQL 8.0+, utf8mb4)
-- 시간 컬럼은 DATETIME(6), 애플리케이션 타임존 Asia/Seoul 기준으로 저장한다.

-- ─────────────────────────────────────────
-- 회원
-- ─────────────────────────────────────────
CREATE TABLE users (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    provider         VARCHAR(10)  NOT NULL COMMENT 'KAKAO, APPLE',
    provider_id      VARCHAR(100) NOT NULL COMMENT '소셜 제공자 사용자 식별자',
    nickname         VARCHAR(20)  NULL     COMMENT '프로필 설정 전에는 NULL',
    child_age_group  VARCHAR(20)  NULL     COMMENT 'PREGNANCY, AGE_0_1, AGE_1_2, AGE_3_5',
    role             VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT 'USER, ADMIN',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, SUSPENDED, BANNED, WITHDRAWN',
    onboarding_step  VARCHAR(20)  NOT NULL DEFAULT 'TERMS' COMMENT 'TERMS, PROFILE, REGION, DONE',
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    withdrawn_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_provider (provider, provider_id),
    UNIQUE KEY uk_users_nickname (nickname)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '회원';

CREATE TABLE user_agreements (
    user_id        BIGINT      NOT NULL,
    terms_code     VARCHAR(30) NOT NULL COMMENT 'SERVICE, PRIVACY, LOCATION, MARKETING',
    terms_version  VARCHAR(10) NOT NULL,
    agreed         BOOLEAN     NOT NULL,
    agreed_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, terms_code, terms_version),
    CONSTRAINT fk_user_agreements_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '약관 동의 이력';

CREATE TABLE refresh_tokens (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    token_hash  CHAR(64)    NOT NULL COMMENT 'SHA-256 hex',
    expires_at  DATETIME(6) NOT NULL,
    revoked_at  DATETIME(6) NULL,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT 'Refresh 토큰';

-- ─────────────────────────────────────────
-- 지역
-- ─────────────────────────────────────────
CREATE TABLE regions (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    parent_id  BIGINT      NULL,
    code       VARCHAR(10) NOT NULL COMMENT '행정동 코드',
    name       VARCHAR(50) NOT NULL COMMENT '예: 망원1동',
    full_name  VARCHAR(100) NOT NULL COMMENT '예: 서울특별시 마포구 망원1동',
    level      TINYINT     NOT NULL COMMENT '1 시도, 2 시군구, 3 읍면동',
    active     BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '서비스 오픈 지역 여부',
    PRIMARY KEY (id),
    UNIQUE KEY uk_regions_code (code),
    KEY idx_regions_parent (parent_id),
    KEY idx_regions_name (name),
    CONSTRAINT fk_regions_parent FOREIGN KEY (parent_id) REFERENCES regions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '행정 지역';

CREATE TABLE user_regions (
    user_id     BIGINT      NOT NULL,
    region_id   BIGINT      NOT NULL,
    is_primary  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, region_id),
    KEY idx_user_regions_region (region_id),
    CONSTRAINT fk_user_regions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_regions_region FOREIGN KEY (region_id) REFERENCES regions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '회원 관심 지역 (최대 3개)';

-- ─────────────────────────────────────────
-- 장소·후기
-- ─────────────────────────────────────────
CREATE TABLE places (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    region_id       BIGINT        NOT NULL,
    name            VARCHAR(100)  NOT NULL,
    category        VARCHAR(30)   NOT NULL COMMENT 'KIDS_CAFE, PEDIATRICS, PLAYGROUND, NURSING_ROOM, CULTURE_CENTER, RESTAURANT',
    address         VARCHAR(200)  NOT NULL,
    lat             DECIMAL(10, 7) NULL,
    lng             DECIMAL(10, 7) NULL,
    kakao_place_id  VARCHAR(30)   NULL,
    source          VARCHAR(10)   NOT NULL COMMENT 'ADMIN, USER',
    status          VARCHAR(20)   NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE, HIDDEN, MERGED',
    merged_into_id  BIGINT        NULL,
    created_by      BIGINT        NULL,
    verified_at     DATE          NULL COMMENT '정보 마지막 확인일',
    review_count    INT           NOT NULL DEFAULT 0,
    post_count      INT           NOT NULL DEFAULT 0,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_places_kakao (kakao_place_id),
    KEY idx_places_region_category (region_id, category, status),
    FULLTEXT KEY ftx_places_name (name) WITH PARSER ngram,
    CONSTRAINT fk_places_region FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT fk_places_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '장소';

CREATE TABLE place_amenities (
    place_id         BIGINT      NOT NULL,
    amenity          VARCHAR(20) NOT NULL COMMENT 'PARKING, STROLLER, NURSING_ROOM, DIAPER_TABLE',
    status           VARCHAR(10) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'YES, NO, UNKNOWN (후기 집계 결과)',
    confirmed_count  INT         NOT NULL DEFAULT 0,
    updated_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (place_id, amenity),
    CONSTRAINT fk_place_amenities_place FOREIGN KEY (place_id) REFERENCES places (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '장소 편의시설 집계';

CREATE TABLE place_reviews (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    place_id         BIGINT      NOT NULL,
    user_id          BIGINT      NOT NULL,
    visited_on       CHAR(7)     NOT NULL COMMENT 'YYYY-MM',
    child_age_group  VARCHAR(20) NOT NULL,
    content          TEXT        NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE, HIDDEN, DELETED',
    reaction_count   INT         NOT NULL DEFAULT 0,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_place_reviews_visit (place_id, user_id, visited_on),
    KEY idx_place_reviews_place (place_id, status, id),
    KEY idx_place_reviews_user (user_id, id),
    CONSTRAINT fk_place_reviews_place FOREIGN KEY (place_id) REFERENCES places (id),
    CONSTRAINT fk_place_reviews_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '장소 후기 (회원·장소·방문월당 1건)';

CREATE TABLE review_amenity_checks (
    review_id  BIGINT      NOT NULL,
    amenity    VARCHAR(20) NOT NULL,
    value      VARCHAR(10) NOT NULL COMMENT 'YES, NO, UNKNOWN',
    PRIMARY KEY (review_id, amenity),
    CONSTRAINT fk_review_amenity_checks_review FOREIGN KEY (review_id) REFERENCES place_reviews (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '후기별 편의시설 체크';

-- ─────────────────────────────────────────
-- 피드
-- ─────────────────────────────────────────
CREATE TABLE posts (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    region_id       BIGINT       NOT NULL,
    category        VARCHAR(20)  NOT NULL COMMENT 'QUESTION, TIP, EVENT, FREE',
    place_id        BIGINT       NULL     COMMENT '장소 태그 (선택)',
    title           VARCHAR(100) NOT NULL,
    content         TEXT         NOT NULL,
    age_group       VARCHAR(20)  NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE, HIDDEN, DELETED',
    reaction_count  INT          NOT NULL DEFAULT 0,
    comment_count   INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_posts_region_feed (region_id, status, id),
    KEY idx_posts_region_category (region_id, category, status, id),
    KEY idx_posts_place (place_id, status, id),
    KEY idx_posts_user (user_id, id),
    FULLTEXT KEY ftx_posts_text (title, content) WITH PARSER ngram,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_posts_region FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT fk_posts_place FOREIGN KEY (place_id) REFERENCES places (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '피드 글';

CREATE TABLE comments (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    post_id     BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    parent_id   BIGINT        NULL COMMENT '대댓글이면 상위 댓글 (1단계만)',
    content     VARCHAR(1000) NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE, HIDDEN, DELETED',
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    KEY idx_comments_post (post_id, id),
    KEY idx_comments_parent (parent_id),
    KEY idx_comments_user (user_id, id),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '댓글';

CREATE TABLE reactions (
    user_id      BIGINT      NOT NULL,
    target_type  VARCHAR(20) NOT NULL COMMENT 'POST, COMMENT, REVIEW',
    target_id    BIGINT      NOT NULL,
    type         VARCHAR(10) NOT NULL DEFAULT 'LIKE',
    created_at   DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, target_type, target_id, type),
    KEY idx_reactions_target (target_type, target_id),
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '공감';

CREATE TABLE images (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    image_key    VARCHAR(255) NOT NULL COMMENT '오브젝트 스토리지 키',
    uploader_id  BIGINT       NOT NULL,
    target_type  VARCHAR(20)  NULL COMMENT 'POST, REVIEW, PLACE (업로드 직후 NULL)',
    target_id    BIGINT       NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_images_key (image_key),
    KEY idx_images_target (target_type, target_id, sort_order),
    CONSTRAINT fk_images_uploader FOREIGN KEY (uploader_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '이미지';

-- ─────────────────────────────────────────
-- 안전·운영
-- ─────────────────────────────────────────
CREATE TABLE reports (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    reporter_id  BIGINT        NOT NULL,
    target_type  VARCHAR(20)   NOT NULL COMMENT 'POST, COMMENT, REVIEW, USER',
    target_id    BIGINT        NOT NULL,
    reason       VARCHAR(20)   NOT NULL COMMENT 'SPAM_AD, ABUSE, PRIVACY, FALSE_INFO, CHILD_SAFETY, ETC',
    detail       VARCHAR(500)  NULL,
    status       VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, RESOLVED, REJECTED',
    action       VARCHAR(20)   NULL COMMENT 'HIDE, DELETE, SANCTION, REJECT',
    memo         VARCHAR(500)  NULL,
    handled_by   BIGINT        NULL,
    handled_at   DATETIME(6)   NULL,
    created_at   DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reports_once (reporter_id, target_type, target_id),
    KEY idx_reports_status (status, id),
    KEY idx_reports_target (target_type, target_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_reports_handled_by FOREIGN KEY (handled_by) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '신고';

CREATE TABLE blocks (
    user_id          BIGINT      NOT NULL,
    blocked_user_id  BIGINT      NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, blocked_user_id),
    CONSTRAINT fk_blocks_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_blocks_blocked FOREIGN KEY (blocked_user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '사용자 차단';

CREATE TABLE sanctions (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(20)  NOT NULL COMMENT 'WARNING, SUSPEND_7D, SUSPEND_30D, BAN',
    reason       VARCHAR(500) NOT NULL,
    report_id    BIGINT       NULL,
    starts_at    DATETIME(6)  NOT NULL,
    ends_at      DATETIME(6)  NULL COMMENT 'BAN이면 NULL',
    released_at  DATETIME(6)  NULL,
    created_by   BIGINT       NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_sanctions_user (user_id, id),
    CONSTRAINT fk_sanctions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_sanctions_report FOREIGN KEY (report_id) REFERENCES reports (id),
    CONSTRAINT fk_sanctions_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '회원 제재';

CREATE TABLE inquiries (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    user_id      BIGINT        NOT NULL,
    type         VARCHAR(20)   NOT NULL COMMENT 'ACCOUNT, BUG, SUGGESTION, PARTNERSHIP, ETC',
    content      VARCHAR(2000) NOT NULL,
    reply_email  VARCHAR(100)  NULL,
    status       VARCHAR(20)   NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, ANSWERED, CLOSED',
    created_at   DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    KEY idx_inquiries_status (status, id),
    CONSTRAINT fk_inquiries_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT '문의';
