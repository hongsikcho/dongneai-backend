package com.dongneai.review.domain;

import com.dongneai.shared.code.Amenity;
import com.dongneai.shared.code.AmenityStatus;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 장소의 이용 조건 체크 집계. 회원마다 가장 최근 방문(같은 달이면 나중에 쓴) 공개 후기 하나만 센다.
 * '모름'과 체크 안 함은 세지 않는다.
 */
public final class AmenityTally {

    public record Count(int yes, int no) {
    }

    private static final Comparator<Review> LATEST =
            Comparator.comparing(Review::getVisitedOn).thenComparing(Review::getId);

    private AmenityTally() {
    }

    public static Map<Amenity, Count> of(List<Review> reviews) {
        Map<Long, Review> latestByUser = reviews.stream()
                .filter(Review::isVisible)
                .collect(Collectors.toMap(Review::getUserId, r -> r, (a, b) -> LATEST.compare(a, b) >= 0 ? a : b));

        Map<Amenity, int[]> counts = new EnumMap<>(Amenity.class);
        for (Review review : latestByUser.values()) {
            review.getAmenityChecks().forEach((amenity, status) -> {
                int[] c = counts.computeIfAbsent(amenity, a -> new int[2]);
                if (status == AmenityStatus.YES) {
                    c[0]++;
                } else if (status == AmenityStatus.NO) {
                    c[1]++;
                }
            });
        }
        Map<Amenity, Count> result = new EnumMap<>(Amenity.class);
        counts.forEach((amenity, c) -> result.put(amenity, new Count(c[0], c[1])));
        return result;
    }
}
