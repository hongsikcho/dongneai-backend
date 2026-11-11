package com.dongneai.shared.presentation;

import com.dongneai.shared.code.CodeTable;
import com.dongneai.shared.code.CodeTable.Code;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 앱 공통 코드표 (비로그인 허용). 앱은 이 라벨을 그대로 표시한다.
 * 각 모듈이 제공하는 {@link CodeTable} 빈을 모아 응답한다 (ageGroups, placeCategories, amenities …).
 */
@RestController
@RequiredArgsConstructor
public class MetaController {

    private final List<CodeTable> codeTables;

    @GetMapping("/api/v1/meta/codes")
    public Map<String, List<Code>> codes() {
        Map<String, List<Code>> result = new LinkedHashMap<>();
        codeTables.forEach(table -> result.putAll(table.codes()));
        return result;
    }
}
