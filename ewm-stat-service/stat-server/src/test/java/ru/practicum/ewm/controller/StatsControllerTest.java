package ru.practicum.ewm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.ViewsStatsRequest;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsService statsService;

    private EndpointHit endpointHit;
    private ViewStats viewStats;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String startStr;
    private String endStr;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now;
        startStr = start.format(formatter);
        endStr = end.format(formatter);

        endpointHit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        viewStats = ViewStats.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(5L)
                .build();
    }

    @Test
    void hit_ShouldReturnCreatedStatus() throws Exception {
        doNothing().when(statsService).saveHit(any(EndpointHit.class));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointHit)))
                .andExpect(status().isCreated());
    }

    @Test
    void getStats_ShouldReturnOkStatus() throws Exception {
        List<ViewStats> stats = List.of(viewStats);
        when(statsService.getViewStatsList(any(ViewsStatsRequest.class)))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value(viewStats.getApp()))
                .andExpect(jsonPath("$[0].uri").value(viewStats.getUri()))
                .andExpect(jsonPath("$[0].hits").value(viewStats.getHits()));
    }

    @Test
    void hit_WithMinimalData_ShouldReturnCreatedStatus() throws Exception {
        EndpointHit minimalHit = EndpointHit.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        doNothing().when(statsService).saveHit(any(EndpointHit.class));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalHit)))
                .andExpect(status().isCreated());
    }

    @Test
    void getStats_WithoutUris_ShouldReturnOkStatus() throws Exception {
        List<ViewStats> stats = List.of(viewStats);
        when(statsService.getViewStatsList(any(ViewsStatsRequest.class)))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr))
                .andExpect(status().isOk());
    }
}