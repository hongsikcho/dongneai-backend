package com.dongneai.region;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** 로컬 MySQL 필요: docker compose up -d 후 ./gradlew integrationTest */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthAndRegionApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 토큰_없이_보호된_API를_호출하면_401_JSON() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void 잘못된_토큰은_401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 개발용_토큰으로_내_정보를_조회한다() throws Exception {
        String body = mockMvc.perform(post("/api/v1/dev/token").param("devId", "it-user"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode token = objectMapper.readTree(body);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(token.get("userId").asLong()))
                .andExpect(jsonPath("$.onboardingStep").value("TERMS"));
    }

    @Test
    void 지역_검색은_오픈된_동만_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/regions").param("q", "안양"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("안양시"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void 지역_검색어가_비면_400() throws Exception {
        mockMvc.perform(get("/api/v1/regions").param("q", " "))
                .andExpect(status().isBadRequest());
    }
}
