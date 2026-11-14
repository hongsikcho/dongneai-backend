-- 서비스 지역을 행정동에 한정하지 않는다 (시·구·동 어느 단위든 active면 서비스 지역).
-- 장소 등록 검색은 지역 중심 좌표에서 search_radius_m 반경 안을 찾는다.
ALTER TABLE regions
    ADD COLUMN lat DECIMAL(10, 7) NULL COMMENT '지역 중심 위도' AFTER level,
    ADD COLUMN lng DECIMAL(10, 7) NULL COMMENT '지역 중심 경도' AFTER lat,
    ADD COLUMN search_radius_m INT NOT NULL DEFAULT 2000 COMMENT '장소 검색 반경(m), 최대 20000' AFTER lng;
