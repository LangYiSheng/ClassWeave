package com.lanchen.classweave;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void registerAndCreateScheduleFlowWorks() throws Exception {
        String token = register("tester", "12345678", "Tester");

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("tester"));

        mockMvc.perform(put("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Tester Renamed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.displayName").value("Tester Renamed"));

        MvcResult createScheduleResult = mockMvc.perform(post("/api/v1/schedules")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "大三下课程表",
                                  "termLabel": "2026 春",
                                  "description": "主课表",
                                  "startDate": "2026-02-24",
                                  "totalWeeks": 18,
                                  "maxPeriodsPerDay": 12,
                                  "defaultColor": "#1F6FEB"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        Long scheduleId = objectMapper.readTree(createScheduleResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(put("/api/v1/schedules/{scheduleId}/display-name", scheduleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayNameOverride": "我的主课表"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/api/v1/schedules/{scheduleId}/display-settings", scheduleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayColor": "#DF5D43",
                                  "displayOpacity": 0.75,
                                  "isVisibleDefault": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "12345678",
                                  "newPassword": "87654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "tester",
                                  "password": "87654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void viewerCanRemoveAcceptedSharedSchedule() throws Exception {
        String ownerToken = register("owner", "12345678", "Owner");
        MvcResult createScheduleResult = mockMvc.perform(post("/api/v1/schedules")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "共享课表",
                                  "termLabel": "2026 春",
                                  "description": "用于共享",
                                  "startDate": "2026-02-24",
                                  "totalWeeks": 18,
                                  "maxPeriodsPerDay": 12,
                                  "defaultColor": "#1F6FEB"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        Long scheduleId = objectMapper.readTree(createScheduleResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        MvcResult shareResult = mockMvc.perform(post("/api/v1/schedules/{scheduleId}/share-links", scheduleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expiresAt": null
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String shareToken = objectMapper.readTree(shareResult.getResponse().getContentAsString())
                .path("data")
                .path("shareToken")
                .asText();

        String viewerToken = register("viewer", "12345678", "Viewer");
        mockMvc.perform(post("/api/v1/shares/{shareToken}/accept", shareToken)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/v1/schedules/{scheduleId}/access", scheduleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    private String register(String username, String password, String displayName) throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "displayName": "%s"
                                }
                                """.formatted(username, password, displayName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.username").value(username))
                .andReturn();

        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = registerJson.path("data").path("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

}
