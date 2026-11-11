-- 장소 편의시설을 후기 체크로 집계한다.
-- yes_count / no_count: 사람별 최신 후기 기준 '있음'·'없음' 수
-- admin_status: 운영자 입력값. 후기 체크가 동률이거나 없을 때 쓴다.
-- status / confirmed_count 는 위 값으로 계산한 결과 (목록 필터·표시용)
ALTER TABLE place_amenities
    ADD COLUMN admin_status VARCHAR(10) NULL COMMENT 'YES, NO, UNKNOWN (운영자 입력)' AFTER amenity,
    ADD COLUMN yes_count INT NOT NULL DEFAULT 0 COMMENT '후기 체크 있음 수' AFTER status,
    ADD COLUMN no_count INT NOT NULL DEFAULT 0 COMMENT '후기 체크 없음 수' AFTER yes_count;

-- 지금까지의 값은 모두 운영자 입력(시드 포함)이다
UPDATE place_amenities SET admin_status = status;
