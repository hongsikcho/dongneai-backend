package com.dongneai.place.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 카카오 REST API 키는 환경변수 KAKAO_REST_API_KEY로만 주입한다. 비어 있으면 장소 검색은 503. */
@ConfigurationProperties(prefix = "app.kakao")
public record KakaoLocalProperties(String restApiKey, String baseUrl) {

    public boolean configured() {
        return restApiKey != null && !restApiKey.isBlank();
    }
}
