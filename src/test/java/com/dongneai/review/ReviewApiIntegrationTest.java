package com.dongneai.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * 후기 API. 로컬 MySQL 필요. 매 실행마다 새 회원(UUID)으로 쓰므로 반복 실행해도 된다.
 * 장소 5(안양시 주민센터 수유실)에 쓰고, 후기 수는 실행 전 값과의 차이로 검증한다.
 * 장소 목록 정렬 테스트가 시드의 후기 수에 기대므로, 만든 후기는 테스트가 끝나면 지운다.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ReviewApiIntegrationTest {

    private static final long PLACE_ID = 5;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private final List<String[]> created = new ArrayList<>();
    private String writer;
    private String other;
    private final String thisMonth = YearMonth.now(SEOUL).toString();
    private final String lastMonth = YearMonth.now(SEOUL).minusMonths(1).toString();

    @BeforeEach
    void setUp() throws Exception {
        writer = token("review-it-" + UUID.randomUUID());
        other = token("review-it-" + UUID.randomUUID());
    }

    @AfterEach
    void cleanUp() throws Exception {
        for (String[] tokenAndId : created) {
            mockMvc.perform(auth(delete("/api/v1/reviews/" + tokenAndId[1]), tokenAndId[0]));
        }
    }

    @Test
    void 후기를_쓰면_목록에_보이고_장소_후기수와_별점에_반영된다() throws Exception {
        int before = reviewCount();

        String response = mockMvc.perform(auth(post("/api/v1/places/" + PLACE_ID + "/reviews"), writer)
                        .content(body(thisMonth, 4, "수유실이 깨끗하고 기저귀 교환대도 있었어요.")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.amenities.NURSING_ROOM").value("YES"))
                .andExpect(jsonPath("$.mine").value(true))
                .andReturn().getResponse().getContentAsString();
        long reviewId = json(response).get("id").asLong();
        created.add(new String[] {writer, String.valueOf(reviewId)});

        assertThat(reviewCount()).isEqualTo(before + 1);
        mockMvc.perform(auth(get("/api/v1/places/" + PLACE_ID), writer))
                .andExpect(jsonPath("$.ratingAvg").isNumber());

        mockMvc.perform(auth(get("/api/v1/places/" + PLACE_ID + "/reviews"), other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(reviewId))
                .andExpect(jsonPath("$.items[0].mine").value(false));
    }

    @Test
    void 같은_달_후기는_한_번만_쓸_수_있다() throws Exception {
        long reviewId = create(writer, thisMonth, 5);

        mockMvc.perform(auth(post("/api/v1/places/" + PLACE_ID + "/reviews"), writer)
                        .content(body(thisMonth, 3, "한 번 더 써 보는 같은 달 후기입니다.")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVIEW_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.details.reviewId").value(reviewId));

        // 다른 달 방문이면 쓸 수 있다
        create(writer, lastMonth, 3);
    }

    @Test
    void 미래_달은_방문_월로_고를_수_없다() throws Exception {
        String nextMonth = YearMonth.now(SEOUL).plusMonths(1).toString();
        mockMvc.perform(auth(post("/api/v1/places/" + PLACE_ID + "/reviews"), writer)
                        .content(body(nextMonth, 3, "아직 오지 않은 달의 방문 후기입니다.")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_VISIT_MONTH"));
    }

    @Test
    void 작성자만_수정_삭제할_수_있고_삭제하면_같은_달에_다시_쓸_수_있다() throws Exception {
        long reviewId = create(writer, thisMonth, 2);

        mockMvc.perform(auth(patch("/api/v1/reviews/" + reviewId), other)
                        .content(body(thisMonth, 5, "남의 후기를 고치려는 시도입니다.")))
                .andExpect(status().isForbidden());

        mockMvc.perform(auth(patch("/api/v1/reviews/" + reviewId), writer)
                        .content(body(thisMonth, 5, "다시 가 보니 훨씬 좋아졌어요. 별점 올립니다.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));

        int before = reviewCount();
        mockMvc.perform(auth(delete("/api/v1/reviews/" + reviewId), other)).andExpect(status().isForbidden());
        mockMvc.perform(auth(delete("/api/v1/reviews/" + reviewId), writer)).andExpect(status().isNoContent());
        assertThat(reviewCount()).isEqualTo(before - 1);
        mockMvc.perform(auth(get("/api/v1/reviews/" + reviewId), writer)).andExpect(status().isNotFound());

        create(writer, thisMonth, 4);
        assertThat(reviewCount()).isEqualTo(before);
    }

    @Test
    void 후기_체크가_장소_편의시설_필터에_반영된다() throws Exception {
        // 장소 5에는 운영자가 입력한 유모차 정보가 없다
        assertThat(placeIdsWithAmenity("STROLLER")).doesNotContain(PLACE_ID);

        String response = mockMvc.perform(auth(post("/api/v1/places/" + PLACE_ID + "/reviews"), writer)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "visitedOn", thisMonth,
                                "childAgeGroup", "AGE_1_2",
                                "rating", 5,
                                "content", "유모차 끌고 들어가기 편했어요. 엘리베이터도 있어요.",
                                "amenities", Map.of("STROLLER", "YES")))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long reviewId = json(response).get("id").asLong();

        assertThat(placeIdsWithAmenity("STROLLER")).contains(PLACE_ID);
        mockMvc.perform(auth(get("/api/v1/places/" + PLACE_ID), writer))
                .andExpect(jsonPath("$.amenities[?(@.amenity == 'STROLLER')].status").value(contains("YES")))
                .andExpect(jsonPath("$.amenities[?(@.amenity == 'STROLLER')].confirmedCount").value(contains(1)));

        // 후기를 지우면 다시 빠진다
        mockMvc.perform(auth(delete("/api/v1/reviews/" + reviewId), writer)).andExpect(status().isNoContent());
        assertThat(placeIdsWithAmenity("STROLLER")).doesNotContain(PLACE_ID);
    }

    @Test
    void 없는_장소에는_후기를_쓸_수_없다() throws Exception {
        mockMvc.perform(auth(post("/api/v1/places/999999/reviews"), writer)
                        .content(body(thisMonth, 3, "존재하지 않는 장소에 쓰는 후기입니다.")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE_NOT_FOUND"));
    }

    private long create(String token, String visitedOn, int rating) throws Exception {
        String response = mockMvc.perform(auth(post("/api/v1/places/" + PLACE_ID + "/reviews"), token)
                        .content(body(visitedOn, rating, "통합 테스트로 작성한 후기 본문입니다.")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long reviewId = json(response).get("id").asLong();
        created.add(new String[] {token, String.valueOf(reviewId)});
        return reviewId;
    }

    private List<Long> placeIdsWithAmenity(String amenity) throws Exception {
        String response = mockMvc.perform(auth(get("/api/v1/places"), writer)
                        .param("regionId", "3")
                        .param("amenities", amenity)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Long> ids = new ArrayList<>();
        json(response).get("items").forEach(item -> ids.add(item.get("id").asLong()));
        return ids;
    }

    private int reviewCount() throws Exception {
        String response = mockMvc.perform(auth(get("/api/v1/places/" + PLACE_ID), writer))
                .andReturn().getResponse().getContentAsString();
        return json(response).get("reviewCount").asInt();
    }

    private String body(String visitedOn, int rating, String content) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "visitedOn", visitedOn,
                "childAgeGroup", "AGE_0_1",
                "rating", rating,
                "content", content,
                "amenities", Map.of("NURSING_ROOM", "YES", "PARKING", "UNKNOWN")));
    }

    private String token(String devId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/dev/token").param("devId", devId))
                .andReturn().getResponse().getContentAsString();
        return json(response).get("accessToken").asText();
    }

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }
}
