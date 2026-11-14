package com.dongneai.region.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer level;

    /** 지역 중심 좌표. 장소 등록 검색의 기준점. 없으면 지역 이름으로만 검색한다. */
    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "search_radius_m", nullable = false)
    private int searchRadiusM;

    /** 서비스 오픈 지역 여부. 시·구·동 어느 단위든 active면 회원이 고를 수 있는 서비스 지역이다. */
    @Column(nullable = false)
    private boolean active;

    /**
     * 행정구역 코드(예: 카카오 좌표→행정동 코드 "4117352000")가 속한 서비스 지역 중 가장 좁은 곳.
     * 지역 코드는 행정구역 코드의 앞자리(시 "4117", 동 "4117352000")로 저장되어 있다.
     */
    public static Optional<Region> mostSpecificOpenRegion(List<Region> regions, String areaCode) {
        if (areaCode == null || areaCode.isBlank()) {
            return Optional.empty();
        }
        return regions.stream()
                .filter(Region::isActive)
                .filter(r -> areaCode.startsWith(r.getCode()))
                .max(Comparator.comparingInt(r -> r.getCode().length()));
    }
}
