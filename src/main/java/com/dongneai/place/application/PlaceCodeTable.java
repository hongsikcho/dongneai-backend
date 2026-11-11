package com.dongneai.place.application;

import com.dongneai.place.domain.Amenity;
import com.dongneai.place.domain.PlaceCategory;
import com.dongneai.shared.code.CodeTable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 장소 카테고리·편의시설 코드표. */
@Order(10)
@Component
class PlaceCodeTable implements CodeTable {

    @Override
    public Map<String, List<Code>> codes() {
        Map<String, List<Code>> codes = new LinkedHashMap<>();
        codes.put("placeCategories",
                Arrays.stream(PlaceCategory.values()).map(c -> new Code(c.name(), c.getLabel())).toList());
        codes.put("amenities",
                Arrays.stream(Amenity.values()).map(a -> new Code(a.name(), a.getLabel())).toList());
        return codes;
    }
}
