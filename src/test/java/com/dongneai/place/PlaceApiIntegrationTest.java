package com.dongneai.place;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
 * 장소 API. 로컬 시드(R__local_seed.sql)의 장소 1~6을 기준으로 검증한다. 로컬 MySQL 필요.
 * 안양시(3): 1 숲속 실내놀이터(후기 23), 2 꼬마숲(11), 3 햇살 소아청소년과(41), 4 놀이터(4), 5 수유실(0)
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PlaceApiIntegrationTest {

    private static final long ANYANG = 3L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void tokens() throws Exception {
        userToken = token("it-place-" + UUID.randomUUID(), false);
        adminToken = token("it-admin", true);
    }

    @Test
    void 카테고리와_편의시설로_거르고_후기순으로_정렬한다() throws Exception {
        mockMvc.perform(auth(get("/api/v1/places"), userToken)
                        .param("regionId", String.valueOf(ANYANG))
                        .param("category", "KIDS_CAFE")
                        .param("sort", "REVIEWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("숲속 실내놀이터"))
                .andExpect(jsonPath("$.items[0].amenities[0]").value("PARKING"));

        mockMvc.perform(auth(get("/api/v1/places"), userToken)
                        .param("regionId", String.valueOf(ANYANG))
                        .param("amenities", "PARKING,STROLLER")
                        .param("sort", "REVIEWS"))
                .andExpect(jsonPath("$.items[0].id").value(3))
                .andExpect(jsonPath("$.items[1].id").value(1));
    }

    @Test
    void 커서로_다음_페이지를_이어서_조회한다() throws Exception {
        String body = mockMvc.perform(auth(get("/api/v1/places"), userToken)
                        .param("regionId", String.valueOf(ANYANG))
                        .param("sort", "REVIEWS")
                        .param("size", "2"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.nextCursor").value("23_1"))
                .andReturn().getResponse().getContentAsString();
        String cursor = objectMapper.readTree(body).get("nextCursor").asText();

        mockMvc.perform(auth(get("/api/v1/places"), userToken)
                        .param("regionId", String.valueOf(ANYANG))
                        .param("sort", "REVIEWS")
                        .param("size", "2")
                        .param("cursor", cursor))
                .andExpect(jsonPath("$.items[0].id").value(2));
    }

    @Test
    void 장소_상세는_편의시설_4개를_모두_돌려준다() throws Exception {
        mockMvc.perform(auth(get("/api/v1/places/2"), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region.name").value("안양시"))
                .andExpect(jsonPath("$.amenities.length()").value(4))
                .andExpect(jsonPath("$.amenities[?(@.amenity=='PARKING')].status").value("NO"))
                .andExpect(jsonPath("$.amenities[?(@.amenity=='NURSING_ROOM')].status").value("UNKNOWN"));

        mockMvc.perform(auth(get("/api/v1/places/999999"), userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE_NOT_FOUND"));
    }

    @Test
    void 같은_카카오_장소는_두_번_등록할_수_없다() throws Exception {
        String kakaoId = "it" + System.nanoTime() % 1_000_000_000L;
        String body = """
                {"regionId":%d,"name":"테스트 장소","category":"PLAYGROUND","address":"행복로 99","kakaoPlaceId":"%s"}
                """.formatted(ANYANG, kakaoId);

        String created = mockMvc.perform(auth(post("/api/v1/places"), userToken).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("USER"))
                .andExpect(jsonPath("$.verifiedAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long placeId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(auth(post("/api/v1/places"), userToken).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLACE_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.details.placeId").value(placeId));
    }

    @Test
    void 술집_같은_부적합_업종은_등록할_수_없다() throws Exception {
        String body = """
                {"regionId":%d,"name":"테스트 호프","category":"RESTAURANT","address":"안양시 (가상) 1",
                 "kakaoPlaceId":"it-bar-%d","kakaoCategoryName":"음식점 > 술집 > 호프,요리주점"}
                """.formatted(ANYANG, System.nanoTime() % 1_000_000_000L);

        mockMvc.perform(auth(post("/api/v1/places"), userToken).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLACE_NOT_ALLOWED"));
    }

    @Test
    void 일반_회원은_관리자_API를_쓸_수_없다() throws Exception {
        mockMvc.perform(auth(get("/api/v1/admin/places"), userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_편의시설과_함께_시딩하고_숨길_수_있다() throws Exception {
        String created = mockMvc.perform(auth(post("/api/v1/admin/places"), adminToken).content("""
                        {"regionId":%d,"name":"시딩 키즈카페","category":"KIDS_CAFE","address":"행복로 77",
                         "amenities":{"PARKING":"YES","STROLLER":"NO"},"verified":true}
                        """.formatted(ANYANG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("ADMIN"))
                .andExpect(jsonPath("$.verifiedAt").exists())
                .andExpect(jsonPath("$.amenities[?(@.amenity=='PARKING')].status").value("YES"))
                .andReturn().getResponse().getContentAsString();
        long placeId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(auth(patch("/api/v1/admin/places/" + placeId), adminToken).content("""
                        {"regionId":%d,"name":"시딩 키즈카페","category":"KIDS_CAFE","address":"행복로 77","status":"HIDDEN"}
                        """.formatted(ANYANG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));

        mockMvc.perform(auth(get("/api/v1/places/" + placeId), userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void 코드표는_로그인_없이_조회된다() throws Exception {
        mockMvc.perform(get("/api/v1/meta/codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeCategories[0].label").value("키즈카페"));
    }

    private String token(String devId, boolean admin) throws Exception {
        String body = mockMvc.perform(post("/api/v1/dev/token").param("devId", devId).param("admin", String.valueOf(admin)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }
}
