package com.dongneai.common.api;

import com.dongneai.common.code.AgeGroup;
import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.PlaceCategory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 앱 공통 코드표 (비로그인 허용). 앱은 이 라벨을 그대로 표시한다. */
@RestController
public class MetaController {

    private static final Map<AgeGroup, String> AGE_LABELS = Map.of(
            AgeGroup.PREGNANCY, "임신·출산 준비",
            AgeGroup.AGE_0_1, "0~12개월",
            AgeGroup.AGE_1_2, "1~2세",
            AgeGroup.AGE_3_5, "3~5세");

    public record Code(String code, String label) {
    }

    public record Codes(List<Code> ageGroups, List<Code> placeCategories, List<Code> amenities) {
    }

    @GetMapping("/api/v1/meta/codes")
    public Codes codes() {
        return new Codes(
                Arrays.stream(AgeGroup.values()).map(a -> new Code(a.name(), AGE_LABELS.get(a))).toList(),
                Arrays.stream(PlaceCategory.values()).map(c -> new Code(c.name(), c.getLabel())).toList(),
                Arrays.stream(Amenity.values()).map(a -> new Code(a.name(), a.getLabel())).toList());
    }
}
