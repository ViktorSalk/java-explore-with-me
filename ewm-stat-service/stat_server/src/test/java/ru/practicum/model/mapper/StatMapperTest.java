package ru.practicum.model.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.StatDto;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatMapperTest {

    @Test
    void toStat_ShouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        StatDto statDto = StatDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        Stat result = StatMapper.toStat(statDto);

        assertEquals(statDto.getApp(), result.getApp());
        assertEquals(statDto.getUri(), result.getUri());
        assertEquals(statDto.getIp(), result.getIp());
        assertEquals(statDto.getTimestamp(), result.getTimestamp());
    }

    @Test
    void toStatDto_ShouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Stat stat = Stat.builder()
                .statId(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        StatDto result = StatMapper.toStatDto(stat);

        assertEquals(stat.getApp(), result.getApp());
        assertEquals(stat.getUri(), result.getUri());
        assertEquals(stat.getIp(), result.getIp());
        assertEquals(stat.getTimestamp(), result.getTimestamp());
    }
}