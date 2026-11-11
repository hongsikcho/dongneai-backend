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

-- 운영자 입력값(admin_status). 후기 체크가 생기면 status·confirmed_count는 집계로 다시 계산된다
INSERT IGNORE INTO place_amenities (place_id, amenity, admin_status, status, confirmed_count, updated_at)
VALUES (1, 'PARKING', 'YES', 'YES', 18, NOW(6)), (1, 'STROLLER', 'YES', 'YES', 15, NOW(6)), (1, 'NURSING_ROOM', 'YES', 'YES', 12, NOW(6)),
       (2, 'STROLLER', 'YES', 'YES', 7, NOW(6)), (2, 'DIAPER_TABLE', 'YES', 'YES', 5, NOW(6)), (2, 'PARKING', 'NO', 'NO', 3, NOW(6)),
       (3, 'PARKING', 'YES', 'YES', 20, NOW(6)), (3, 'STROLLER', 'YES', 'YES', 9, NOW(6)),
       (4, 'STROLLER', 'YES', 'YES', 2, NOW(6)),
       (5, 'NURSING_ROOM', 'YES', 'YES', 0, NOW(6)), (5, 'DIAPER_TABLE', 'YES', 'YES', 0, NOW(6)),
       (6, 'PARKING', 'YES', 'YES', 6, NOW(6));

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

-- 피드 글·댓글 (가상 데이터). 행복동(3)
INSERT IGNORE INTO posts (id, user_id, region_id, category, place_id, title, content, age_group, status, reaction_count, comment_count, created_at, updated_at)
VALUES (9001, 9001, 3, 'QUESTION', 1, '숲속 실내놀이터 주말에 많이 붐비나요?', '토요일 오전에 18개월 아이랑 가 보려는데 대기가 있는지 궁금해요.', 'AGE_1_2', 'VISIBLE', 1, 2, NOW(6) - INTERVAL 2 HOUR, NOW(6) - INTERVAL 2 HOUR),
       (9002, 9002, 3, 'TIP', NULL, '행복동 소아과 야간 진료 정리', '평일 저녁 8시까지 하는 곳은 햇살 소아청소년과예요. 토요일은 오후 1시까지.', NULL, 'VISIBLE', 3, 0, NOW(6) - INTERVAL 1 DAY, NOW(6) - INTERVAL 1 DAY),
       (9003, 9003, 3, 'EVENT', NULL, '주민센터 아기 오감놀이 신청 받아요', '이번 달 셋째 주 화요일 오전, 0~12개월 대상이에요. 주민센터 2층에서 선착순 신청.', 'AGE_0_1', 'VISIBLE', 0, 0, NOW(6) - INTERVAL 3 DAY, NOW(6) - INTERVAL 3 DAY);

INSERT IGNORE INTO comments (id, post_id, user_id, parent_id, content, status, created_at, updated_at)
VALUES (9001, 9001, 9003, NULL, '오픈 시간 맞춰 가면 대기 없었어요!', 'VISIBLE', NOW(6) - INTERVAL 1 HOUR, NOW(6) - INTERVAL 1 HOUR),
       (9002, 9001, 9001, 9001, '감사해요 일찍 가 볼게요 :)', 'VISIBLE', NOW(6) - INTERVAL 30 MINUTE, NOW(6) - INTERVAL 30 MINUTE);

INSERT IGNORE INTO reactions (user_id, target_type, target_id, type, created_at)
VALUES (9002, 'POST', 9001, 'LIKE', NOW(6));
