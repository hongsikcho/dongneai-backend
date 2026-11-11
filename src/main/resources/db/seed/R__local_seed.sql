-- 로컬 개발용 시드 (local 프로필에서만 실행). 실제 행정동 코드가 아닌 가상 데이터.
INSERT IGNORE INTO regions (id, parent_id, code, name, full_name, level, active)
VALUES (1, NULL, 'L00', '테스트시', '테스트시', 1, FALSE),
       (2, 1, 'L00A', '테스트구', '테스트시 테스트구', 2, FALSE),
       (3, 2, 'L00A01', '행복동', '테스트시 테스트구 행복동', 3, TRUE),
       (4, 2, 'L00A02', '햇살동', '테스트시 테스트구 햇살동', 3, TRUE);

-- 장소 (가상 데이터). 1~5 행복동, 6 햇살동
INSERT IGNORE INTO places (id, region_id, name, category, address, source, status, verified_at, review_count, post_count, created_at, updated_at)
VALUES (1, 3, '숲속 실내놀이터', 'KIDS_CAFE', '테스트시 테스트구 행복로 10', 'ADMIN', 'VISIBLE', '2026-10-20', 23, 6, NOW(6), NOW(6)),
       (2, 3, '꼬마숲 키즈카페', 'KIDS_CAFE', '테스트시 테스트구 행복로 22', 'ADMIN', 'VISIBLE', '2026-09-28', 11, 2, NOW(6), NOW(6)),
       (3, 3, '햇살 소아청소년과', 'PEDIATRICS', '테스트시 테스트구 행복로 35 2층', 'ADMIN', 'VISIBLE', '2026-10-15', 41, 9, NOW(6), NOW(6)),
       (4, 3, '행복동 근린공원 놀이터', 'PLAYGROUND', '테스트시 테스트구 행복동 100', 'ADMIN', 'VISIBLE', '2026-08-01', 4, 0, NOW(6), NOW(6)),
       (5, 3, '행복동 주민센터 수유실', 'NURSING_ROOM', '테스트시 테스트구 행복로 1 1층', 'ADMIN', 'VISIBLE', NULL, 0, 0, NOW(6), NOW(6)),
       (6, 4, '별빛 문화센터', 'CULTURE_CENTER', '테스트시 테스트구 햇살로 5', 'ADMIN', 'VISIBLE', '2026-10-01', 12, 1, NOW(6), NOW(6));

INSERT IGNORE INTO place_amenities (place_id, amenity, status, confirmed_count, updated_at)
VALUES (1, 'PARKING', 'YES', 18, NOW(6)), (1, 'STROLLER', 'YES', 15, NOW(6)), (1, 'NURSING_ROOM', 'YES', 12, NOW(6)),
       (2, 'STROLLER', 'YES', 7, NOW(6)), (2, 'DIAPER_TABLE', 'YES', 5, NOW(6)), (2, 'PARKING', 'NO', 3, NOW(6)),
       (3, 'PARKING', 'YES', 20, NOW(6)), (3, 'STROLLER', 'YES', 9, NOW(6)),
       (4, 'STROLLER', 'YES', 2, NOW(6)),
       (5, 'NURSING_ROOM', 'YES', 0, NOW(6)), (5, 'DIAPER_TABLE', 'YES', 0, NOW(6)),
       (6, 'PARKING', 'YES', 6, NOW(6));

-- 별점 합계 (가상 데이터). 위 review_count는 목록 정렬 테스트용 가상 값이라 실제 후기 행 수와 다르다.
UPDATE places
SET rating_sum = CASE id WHEN 1 THEN 101 WHEN 2 THEN 46 WHEN 3 THEN 176 WHEN 4 THEN 15 WHEN 6 THEN 53 ELSE rating_sum END
WHERE id IN (1, 2, 3, 4, 6) AND rating_sum = 0;

-- 후기 작성자 (가상 회원). 개발 토큰 회원과 겹치지 않게 provider_id에 seed- 접두사
INSERT IGNORE INTO users (id, provider, provider_id, nickname, child_age_group, role, status, onboarding_step, created_at, updated_at)
VALUES (9001, 'DEV', 'seed-1', '숲속맘', 'AGE_1_2', 'USER', 'ACTIVE', 'DONE', NOW(6), NOW(6)),
       (9002, 'DEV', 'seed-2', '꼬마아빠', 'AGE_3_5', 'USER', 'ACTIVE', 'DONE', NOW(6), NOW(6)),
       (9003, 'DEV', 'seed-3', '행복동새내기', 'AGE_0_1', 'USER', 'ACTIVE', 'DONE', NOW(6), NOW(6));

-- 후기 (가상 데이터). 장소 1 숲속 실내놀이터
INSERT IGNORE INTO place_reviews (id, place_id, user_id, visited_on, child_age_group, rating, content, status, reaction_count, created_at, updated_at)
VALUES (9001, 1, 9001, '2026-10', 'AGE_1_2', 5, '주차가 넓고 유모차 보관 공간이 따로 있어서 편했어요. 평일 오전은 한산합니다.', 'VISIBLE', 0, NOW(6), NOW(6)),
       (9002, 1, 9002, '2026-09', 'AGE_3_5', 4, '4살이 놀기 딱 좋아요. 주말엔 붐비니 오픈 시간에 가는 걸 추천해요.', 'VISIBLE', 0, NOW(6), NOW(6)),
       (9003, 1, 9003, '2026-10', 'AGE_0_1', 4, '수유실이 깔끔했어요. 아기 매트 공간이 작아서 별 하나 뺐어요.', 'VISIBLE', 0, NOW(6), NOW(6));

INSERT IGNORE INTO review_amenity_checks (review_id, amenity, value)
VALUES (9001, 'PARKING', 'YES'), (9001, 'STROLLER', 'YES'),
       (9002, 'PARKING', 'YES'),
       (9003, 'NURSING_ROOM', 'YES'), (9003, 'DIAPER_TABLE', 'NO');
