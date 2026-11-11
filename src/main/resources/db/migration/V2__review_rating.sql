-- 후기 별점 (1~5)과 장소별 별점 합계. 평균 = rating_sum / review_count
ALTER TABLE place_reviews
    ADD COLUMN rating TINYINT NOT NULL COMMENT '별점 1~5' AFTER child_age_group,
    ADD CONSTRAINT chk_place_reviews_rating CHECK (rating BETWEEN 1 AND 5);

ALTER TABLE places
    ADD COLUMN rating_sum INT NOT NULL DEFAULT 0 COMMENT '공개 후기 별점 합계' AFTER review_count;
