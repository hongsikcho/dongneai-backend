package com.dongneai.shared.code;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(0)
@Component
class AgeGroupCodeTable implements CodeTable {

    private static final Map<AgeGroup, String> LABELS = Map.of(
            AgeGroup.PREGNANCY, "임신·출산 준비",
            AgeGroup.AGE_0_1, "0~12개월",
            AgeGroup.AGE_1_2, "1~2세",
            AgeGroup.AGE_3_5, "3~5세");

    @Override
    public Map<String, List<Code>> codes() {
        return Map.of("ageGroups",
                Arrays.stream(AgeGroup.values()).map(a -> new Code(a.name(), LABELS.get(a))).toList());
    }
}
