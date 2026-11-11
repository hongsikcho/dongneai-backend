package com.dongneai.shared.code;

import java.util.List;
import java.util.Map;

/**
 * 앱 공통 코드표 조각. 각 모듈이 자기 코드(예: 장소 카테고리)를 빈으로 제공하면
 * 메타 API가 모아서 응답한다. 키는 응답 JSON의 필드 이름이다.
 */
public interface CodeTable {

    Map<String, List<Code>> codes();

    record Code(String code, String label) {
    }
}
