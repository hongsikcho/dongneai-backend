package com.dongneai.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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

/** 온보딩 전체 흐름: 약관 → 프로필 → 동네 → DONE. 로컬 MySQL 필요. */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OnboardingApiIntegrationTest {

    // R__local_seed.sql: 3 행복동, 4 햇살동 (활성 읍면동), 2 테스트구 (구 단위)
    private static final long HAPPY_DONG = 3L;
    private static final long SUNNY_DONG = 4L;
    private static final long GU = 2L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void signIn() throws Exception {
        String body = mockMvc.perform(post("/api/v1/dev/token").param("devId", "it-" + UUID.randomUUID()))
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("accessToken").asText();
    }

    @Test
    void 약관_프로필_동네를_순서대로_마치면_DONE() throws Exception {
        String nickname = "테스트" + ThreadLocalRandom.current().nextInt(1000, 10000);

        mockMvc.perform(get("/api/v1/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SERVICE"));

        mockMvc.perform(auth(post("/api/v1/users/me/agreements")).content("""
                        {"agreements":[{"code":"SERVICE","agreed":true},{"code":"PRIVACY","agreed":true},{"code":"MARKETING","agreed":false}]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingStep").value("PROFILE"));

        mockMvc.perform(auth(get("/api/v1/users/nickname/check")).param("nickname", nickname))
                .andExpect(jsonPath("$.available").value(true));

        mockMvc.perform(auth(patch("/api/v1/users/me")).content("""
                        {"nickname":"%s","childAgeGroup":"AGE_1_2"}
                        """.formatted(nickname)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.onboardingStep").value("REGION"));

        mockMvc.perform(auth(put("/api/v1/users/me/regions")).content("""
                        {"regions":[{"regionId":%d,"primary":false},{"regionId":%d,"primary":true}]}
                        """.formatted(HAPPY_DONG, SUNNY_DONG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SUNNY_DONG))
                .andExpect(jsonPath("$[0].primary").value(true))
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(auth(get("/api/v1/auth/me")))
                .andExpect(jsonPath("$.onboardingStep").value("DONE"));
    }

    @Test
    void 필수_약관을_빠뜨리면_400() throws Exception {
        mockMvc.perform(auth(post("/api/v1/users/me/agreements")).content("""
                        {"agreements":[{"code":"SERVICE","agreed":true}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUIRED_TERMS_NOT_AGREED"));
    }

    @Test
    void 약관_동의_전에는_프로필을_저장할_수_없다() throws Exception {
        mockMvc.perform(auth(patch("/api/v1/users/me")).content("""
                        {"nickname":"순서위반","childAgeGroup":"AGE_0_1"}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ONBOARDING_REQUIRED"));
    }

    @Test
    void 닉네임_규칙_위반() throws Exception {
        agreeAll();
        mockMvc.perform(auth(get("/api/v1/users/nickname/check")).param("nickname", "관리자"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.reason").value("FORBIDDEN_WORD"));
        mockMvc.perform(auth(patch("/api/v1/users/me")).content("""
                        {"nickname":"a","childAgeGroup":"AGE_0_1"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_NICKNAME"));
    }

    @Test
    void 동네_선택_규칙_위반() throws Exception {
        agreeAll();
        mockMvc.perform(auth(patch("/api/v1/users/me")).content("""
                        {"nickname":"%s","childAgeGroup":"AGE_3_5"}
                        """.formatted("동네" + ThreadLocalRandom.current().nextInt(1000, 10000))));

        // 기본 동네 2개
        mockMvc.perform(auth(put("/api/v1/users/me/regions")).content("""
                        {"regions":[{"regionId":%d,"primary":true},{"regionId":%d,"primary":true}]}
                        """.formatted(HAPPY_DONG, SUNNY_DONG)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REGION_SELECTION"));

        // 동이 아닌 구
        mockMvc.perform(auth(put("/api/v1/users/me/regions")).content("""
                        {"regions":[{"regionId":%d,"primary":true}]}
                        """.formatted(GU)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REGION_NOT_FOUND"));
    }

    private void agreeAll() throws Exception {
        mockMvc.perform(auth(post("/api/v1/users/me/agreements")).content("""
                {"agreements":[{"code":"SERVICE","agreed":true},{"code":"PRIVACY","agreed":true}]}
                """));
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }
}
