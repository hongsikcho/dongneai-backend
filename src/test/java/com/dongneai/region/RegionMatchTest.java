package com.dongneai.region;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongneai.region.domain.Region;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

class RegionMatchTest {

    @Test
    void 행정동_코드가_시작하는_서비스_지역_중_가장_좁은_곳을_고른다() {
        Region gyeonggi = region(1L, "41", false);
        Region anyang = region(3L, "4117", true);
        Region gwanyang = region(9L, "4117352000", true);
        List<Region> regions = List.of(gyeonggi, anyang, gwanyang);

        // 안양시 동안구의 다른 동 → 시 단위 서비스 지역
        assertThat(Region.mostSpecificOpenRegion(regions, "4117353000")).contains(anyang);
        // 동 단위로 따로 연 곳이 있으면 그 동
        assertThat(Region.mostSpecificOpenRegion(regions, "4117352000")).contains(gwanyang);
    }

    @Test
    void 서비스_전_지역이나_코드가_없으면_빈_값() {
        List<Region> regions = List.of(region(1L, "41", false), region(3L, "4117", true));

        assertThat(Region.mostSpecificOpenRegion(regions, "4143051000")).isEmpty(); // 의왕시
        assertThat(Region.mostSpecificOpenRegion(regions, null)).isEmpty();
    }

    private Region region(Long id, String code, boolean active) {
        Region region = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(region, "id", id);
        ReflectionTestUtils.setField(region, "code", code);
        ReflectionTestUtils.setField(region, "active", active);
        return region;
    }
}
