package ru.practicum.ewm.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.model.EndpointHitModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointHitMapperTest {
    private static class EndpointHitMapper {
        public static EndpointHitModel toModel(EndpointHit dto) {
            return EndpointHitModel.builder()
                    .app(dto.getApp())
                    .uri(dto.getUri())
                    .ip(dto.getIp())
                    .timestamp(dto.getTimestamp())
                    .build();
        }

        public static EndpointHit toDto(EndpointHitModel model) {
            return EndpointHit.builder()
                    .id(model.getId())
                    .app(model.getApp())
                    .uri(model.getUri())
                    .ip(model.getIp())
                    .timestamp(model.getTimestamp())
                    .build();
        }
    }

    @Test
    void toModel_ShouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        EndpointHit endpointHit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        EndpointHitModel result = EndpointHitMapper.toModel(endpointHit);

        assertEquals(endpointHit.getApp(), result.getApp());
        assertEquals(endpointHit.getUri(), result.getUri());
        assertEquals(endpointHit.getIp(), result.getIp());
        assertEquals(endpointHit.getTimestamp(), result.getTimestamp());
    }

    @Test
    void toDto_ShouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        EndpointHitModel model = EndpointHitModel.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        EndpointHit result = EndpointHitMapper.toDto(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getApp(), result.getApp());
        assertEquals(model.getUri(), result.getUri());
        assertEquals(model.getIp(), result.getIp());
        assertEquals(model.getTimestamp(), result.getTimestamp());
    }
}