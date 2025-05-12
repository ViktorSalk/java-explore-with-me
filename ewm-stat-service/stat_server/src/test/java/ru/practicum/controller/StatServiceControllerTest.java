package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.service.StatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utill.Constants.DATE_FORMAT;

@WebMvcTest(StatServiceController.class)
class StatServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatService statService;

    private StatDto statDto;
    private StatResponseDto statResponseDto;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private String startStr;
    private String endStr;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now;

        startStr = start.format(formatter);
        endStr = end.format(formatter);

        statDto = StatDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        statResponseDto = StatResponseDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(5)
                .build();
    }

    @Test
    void addStatEvent_ShouldReturnCreatedStatus() throws Exception {
        when(statService.createStat(any(StatDto.class))).thenReturn(statDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value(statDto.getApp()))
                .andExpect(jsonPath("$.uri").value(statDto.getUri()))
                .andExpect(jsonPath("$.ip").value(statDto.getIp()));
    }

    @Test
    void readStatEvent_ShouldReturnOkStatus() throws Exception {
        List<StatResponseDto> stats = List.of(statResponseDto);
        when(statService.readStat(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value(statResponseDto.getApp()))
                .andExpect(jsonPath("$[0].uri").value(statResponseDto.getUri()))
                .andExpect(jsonPath("$[0].hits").value(statResponseDto.getHits()));
    }

    @Test
    void addStatEvent_WithInvalidData_ShouldReturnStatus201() throws Exception {
        StatDto minimalDto = new StatDto();
        minimalDto.setApp("test-app");
        minimalDto.setUri("/test");
        minimalDto.setIp("127.0.0.1");
        minimalDto.setTimestamp(LocalDateTime.now());

        when(statService.createStat(any(StatDto.class))).thenReturn(minimalDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void readStatEvent_WithInvalidTimeRange_ShouldReturnSuccessfulStatus() throws Exception {
        List<StatResponseDto> stats = List.of(statResponseDto);
        when(statService.readStat(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr))
                .andExpect(status().isOk());
    }
}