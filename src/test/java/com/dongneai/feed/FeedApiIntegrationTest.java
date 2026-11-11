package com.dongneai.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
 * 피드 API. 로컬 MySQL 필요. 매번 가입을 마친 새 회원(관심 동네: 행복동)으로 쓰고, 만든 글은 끝나면 지운다.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class FeedApiIntegrationTest {

    private static final long HAPPY_DONG = 3L;
    private static final long SUNNY_DONG = 4L;
    private static final long PLACE_ID = 5L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String writer;
    private String reader;
    private final List<String[]> createdPosts = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        writer = onboardedUser();
        reader = onboardedUser();
    }

    @AfterEach
    void cleanUp() throws Exception {
        for (String[] tokenAndId : createdPosts) {
            mockMvc.perform(auth(delete("/api/v1/posts/" + tokenAndId[1]), tokenAndId[0]));
        }
    }

    @Test
    void 글을_쓰면_피드와_장소_이야기에_보이고_장소_글수가_는다() throws Exception {
        int before = postCount();
        long postId = createPost(writer, "QUESTION", PLACE_ID);

        assertThat(postCount()).isEqualTo(before + 1);
        mockMvc.perform(auth(get("/api/v1/feed").param("regionId", String.valueOf(HAPPY_DONG)), reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].type").value("POST"))
                .andExpect(jsonPath("$.items[0].post.id").value(postId))
                .andExpect(jsonPath("$.items[0].post.place.id").value(PLACE_ID));
        mockMvc.perform(auth(get("/api/v1/places/" + PLACE_ID + "/posts"), reader))
                .andExpect(jsonPath("$.items[0].id").value(postId));
        mockMvc.perform(auth(get("/api/v1/posts/" + postId), reader))
                .andExpect(jsonPath("$.mine").value(false))
                .andExpect(jsonPath("$.region.id").value(HAPPY_DONG));

        // 삭제하면 장소 글 수가 돌아온다
        mockMvc.perform(auth(delete("/api/v1/posts/" + postId), writer)).andExpect(status().isNoContent());
        assertThat(postCount()).isEqualTo(before);
        mockMvc.perform(auth(get("/api/v1/posts/" + postId), reader)).andExpect(status().isNotFound());
    }

    @Test
    void 전체_피드에는_새_후기_카드가_섞이고_카테고리를_고르면_글만_나온다() throws Exception {
        createPost(writer, "TIP", null);
        JsonNode all = json(mockMvc.perform(auth(get("/api/v1/feed")
                        .param("regionId", String.valueOf(HAPPY_DONG)).param("size", "50"), reader))
                .andReturn().getResponse().getContentAsString());
        List<String> types = new ArrayList<>();
        all.get("items").forEach(i -> types.add(i.get("type").asText()));
        assertThat(types).contains("POST", "REVIEW"); // 시드 후기(숲속 실내놀이터)

        JsonNode tips = json(mockMvc.perform(auth(get("/api/v1/feed")
                        .param("regionId", String.valueOf(HAPPY_DONG)).param("category", "TIP"), reader))
                .andReturn().getResponse().getContentAsString());
        tips.get("items").forEach(i -> {
            assertThat(i.get("type").asText()).isEqualTo("POST");
            assertThat(i.get("post").get("category").asText()).isEqualTo("TIP");
        });
    }

    @Test
    void 관심_동네가_아닌_곳에는_글을_쓸_수_없다() throws Exception {
        mockMvc.perform(auth(post("/api/v1/posts"), writer).content(body(SUNNY_DONG, "FREE", null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_MY_REGION"));
    }

    @Test
    void 공감은_여러_번_눌러도_1이고_취소할_수_있다() throws Exception {
        long postId = createPost(writer, "FREE", null);

        mockMvc.perform(auth(put("/api/v1/posts/" + postId + "/like"), reader))
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.reactionCount").value(1));
        mockMvc.perform(auth(put("/api/v1/posts/" + postId + "/like"), reader))
                .andExpect(jsonPath("$.reactionCount").value(1));
        mockMvc.perform(auth(get("/api/v1/posts/" + postId), reader))
                .andExpect(jsonPath("$.liked").value(true));
        mockMvc.perform(auth(delete("/api/v1/posts/" + postId + "/like"), reader))
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.reactionCount").value(0));
    }

    @Test
    void 댓글과_답글을_달고_답글의_답글은_막는다() throws Exception {
        long postId = createPost(writer, "QUESTION", null);

        long commentId = json(mockMvc.perform(auth(post("/api/v1/posts/" + postId + "/comments"), reader)
                        .content("{\"content\":\"저도 궁금해요\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
        long replyId = json(mockMvc.perform(auth(post("/api/v1/posts/" + postId + "/comments"), writer)
                        .content("{\"content\":\"답글이에요\",\"parentId\":" + commentId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(auth(post("/api/v1/posts/" + postId + "/comments"), reader)
                        .content("{\"content\":\"답글의 답글\",\"parentId\":" + replyId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARENT_COMMENT"));

        mockMvc.perform(auth(get("/api/v1/posts/" + postId + "/comments"), reader))
                .andExpect(jsonPath("$[0].id").value(commentId))
                .andExpect(jsonPath("$[0].replies[0].id").value(replyId));
        mockMvc.perform(auth(get("/api/v1/posts/" + postId), reader))
                .andExpect(jsonPath("$.commentCount").value(2));

        // 답글이 있는 댓글을 지우면 자리만 남는다
        mockMvc.perform(auth(delete("/api/v1/comments/" + commentId), writer)).andExpect(status().isForbidden());
        mockMvc.perform(auth(delete("/api/v1/comments/" + commentId), reader)).andExpect(status().isNoContent());
        mockMvc.perform(auth(get("/api/v1/posts/" + postId + "/comments"), reader))
                .andExpect(jsonPath("$[0].deleted").value(true))
                .andExpect(jsonPath("$[0].content").doesNotExist())
                .andExpect(jsonPath("$[0].replies[0].id").value(replyId));
    }

    @Test
    void 작성자만_수정할_수_있고_장소_태그를_바꾸면_글수도_옮겨진다() throws Exception {
        long postId = createPost(writer, "FREE", PLACE_ID);
        int before = postCount();

        mockMvc.perform(auth(patch("/api/v1/posts/" + postId), reader).content(body(HAPPY_DONG, "FREE", null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(auth(patch("/api/v1/posts/" + postId), writer).content(body(HAPPY_DONG, "TIP", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("TIP"))
                .andExpect(jsonPath("$.place").doesNotExist());
        assertThat(postCount()).isEqualTo(before - 1);
    }

    private long createPost(String token, String category, Long placeId) throws Exception {
        String response = mockMvc.perform(auth(post("/api/v1/posts"), token).content(body(HAPPY_DONG, category, placeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = json(response).get("id").asLong();
        createdPosts.add(new String[] {token, String.valueOf(id)});
        return id;
    }

    private int postCount() throws Exception {
        return json(mockMvc.perform(auth(get("/api/v1/places/" + PLACE_ID), writer))
                .andReturn().getResponse().getContentAsString()).get("postCount").asInt();
    }

    private String body(long regionId, String category, Long placeId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("regionId", regionId);
        body.put("category", category);
        body.put("title", "통합 테스트 글 제목");
        body.put("content", "통합 테스트로 작성한 본문입니다.");
        body.put("placeId", placeId);
        return objectMapper.writeValueAsString(body);
    }

    /** 약관 → 프로필 → 관심 동네(행복동)까지 마친 새 회원의 토큰 */
    private String onboardedUser() throws Exception {
        String token = json(mockMvc.perform(post("/api/v1/dev/token").param("devId", "feed-it-" + UUID.randomUUID()))
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();
        mockMvc.perform(auth(post("/api/v1/users/me/agreements"), token).content("""
                {"agreements":[{"code":"SERVICE","agreed":true},{"code":"PRIVACY","agreed":true}]}
                """)).andExpect(status().isOk());
        String nickname = "피드" + ThreadLocalRandom.current().nextInt(100000, 1000000);
        mockMvc.perform(auth(patch("/api/v1/users/me"), token).content("""
                {"nickname":"%s","childAgeGroup":"AGE_1_2"}
                """.formatted(nickname))).andExpect(status().isOk());
        mockMvc.perform(auth(put("/api/v1/users/me/regions"), token).content("""
                {"regions":[{"regionId":%d,"primary":true}]}
                """.formatted(HAPPY_DONG))).andExpect(status().isOk());
        return token;
    }

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }
}
