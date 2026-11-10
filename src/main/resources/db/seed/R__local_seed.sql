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
