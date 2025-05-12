package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class StatServiceRepositoryTest {

    @Autowired
    private StatServiceRepository repository;

    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        end = LocalDateTime.of(2023, 1, 3, 0, 0, 0);
        uris = List.of("/events/1", "/events/2");

        Stat stat1 = Stat.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.of(2023, 1, 2, 12, 0, 0))
                .build();

        Stat stat2 = Stat.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.2")
                .timestamp(LocalDateTime.of(2023, 1, 2, 13, 0, 0))
                .build();

        Stat stat3 = Stat.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.of(2023, 1, 2, 14, 0, 0))
                .build();

        repository.save(stat1);
        repository.save(stat2);
        repository.save(stat3);
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWithUniqueIp() {
        List<StatResponseDto> result = repository.findAllByTimestampBetweenStartAndEndWithUniqueIp(start, end);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWhereIpNotUnique() {
        List<StatResponseDto> result = repository.findAllByTimestampBetweenStartAndEndWhereIpNotUnique(start, end);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWithUrisUniqueIp() {
        List<StatResponseDto> result = repository.findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique() {
        List<StatResponseDto> result = repository.findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(start, end, uris);

        assertNotNull(result);
        assertEquals(3, result.size());
    }
}