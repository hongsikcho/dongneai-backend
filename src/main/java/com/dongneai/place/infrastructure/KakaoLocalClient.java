package com.dongneai.place.infrastructure;

import com.dongneai.place.application.port.ExternalPlaceSearch;
import com.dongneai.place.domain.PlaceErrorCode;
import com.dongneai.shared.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

/** 카카오 로컬 API 키워드 장소 검색. https://developers.kakao.com/docs/latest/ko/local/dev-guide */
@Slf4j
@Component
public class KakaoLocalClient implements ExternalPlaceSearch {

    private final KakaoLocalProperties properties;
    private final RestClient restClient;

    public KakaoLocalClient(KakaoLocalProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl() == null ? "https://dapi.kakao.com" : properties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    private static final int MAX_RADIUS_M = 20_000;

    @Override
    public boolean available() {
        return properties.configured();
    }

    @Override
    public List<ExternalPlace> search(String query, int size, Near near) {
        return searchKeyword(query, size, near).stream()
                .map(d -> new ExternalPlace(d.id(), d.placeName(), d.categoryName(), d.addressName(),
                        d.roadAddressName(), d.x(), d.y()))
                .toList();
    }

    /** 좌표 → 행정구역. region_type이 H(행정동)인 문서의 코드를 쓴다. */
    @Override
    public Optional<String> areaCodeAt(BigDecimal lat, BigDecimal lng) {
        RegionCodeResponse response = call(b -> b.path("/v2/local/geo/coord2regioncode.json")
                .queryParam("x", lng.toPlainString())
                .queryParam("y", lat.toPlainString())
                .build(), RegionCodeResponse.class);
        if (response == null || response.documents() == null) {
            return Optional.empty();
        }
        return response.documents().stream()
                .filter(d -> "H".equals(d.regionType()))
                .map(RegionCodeDocument::code)
                .findFirst();
    }

    public List<Document> searchKeyword(String query, int size, Near near) {
        KeywordResponse response = call(b -> {
            b.path("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("size", size);
            if (near != null && near.lat() != null && near.lng() != null) {
                b.queryParam("x", near.lng().toPlainString())
                        .queryParam("y", near.lat().toPlainString())
                        .queryParam("radius", Math.min(Math.max(near.radiusM(), 100), MAX_RADIUS_M));
            }
            return b.build();
        }, KeywordResponse.class);
        return response == null || response.documents() == null ? List.of() : response.documents();
    }

    private <T> T call(Function<UriBuilder, URI> uri, Class<T> type) {
        if (!properties.configured()) {
            throw new ApiException(PlaceErrorCode.EXTERNAL_SEARCH_UNAVAILABLE,
                    "카카오 REST API 키(KAKAO_REST_API_KEY)가 설정되지 않았습니다.");
        }
        try {
            return restClient.get()
                    .uri(uri)
                    .header("Authorization", "KakaoAK " + properties.restApiKey())
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            // 401: 키 오류, 403: 카카오맵(로컬) 사용 설정 꺼짐 또는 권한 없음
            log.warn("Kakao local API failed: status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new ApiException(PlaceErrorCode.EXTERNAL_SEARCH_UNAVAILABLE,
                    "장소 검색을 지금 사용할 수 없습니다. (카카오 응답 " + e.getStatusCode().value() + ")");
        } catch (RestClientException e) {
            log.warn("Kakao local API failed: {}", e.getMessage());
            throw new ApiException(PlaceErrorCode.EXTERNAL_SEARCH_UNAVAILABLE,
                    "장소 검색을 지금 사용할 수 없습니다. (카카오 연결 실패)");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KeywordResponse(List<Document> documents) {
    }

    /** x = 경도, y = 위도 (문자열) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            String x,
            String y
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegionCodeResponse(List<RegionCodeDocument> documents) {
    }

    /** region_type: B(법정동) 또는 H(행정동). code: 10자리 행정구역 코드. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegionCodeDocument(
            @JsonProperty("region_type") String regionType,
            String code,
            @JsonProperty("address_name") String addressName
    ) {
    }
}
