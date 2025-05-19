package ru.practicum.ewm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.ViewsStatsRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM stats");

        start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        end = LocalDateTime.of(2023, 1, 3, 0, 0, 0);
        uris = List.of("/events/1", "/events/2");

        jdbcTemplate.update(
                "INSERT INTO stats (app, uri, ip, created) VALUES (?, ?, ?, ?)",
                "ewm-main-service", "/events/1", "192.168.1.1", LocalDateTime.of(2023, 1, 2, 12, 0, 0)
        );

        jdbcTemplate.update(
                "INSERT INTO stats (app, uri, ip, created) VALUES (?, ?, ?, ?)",
                "ewm-main-service", "/events/1", "192.168.1.2", LocalDateTime.of(2023, 1, 2, 13, 0, 0)
        );

        jdbcTemplate.update(
                "INSERT INTO stats (app, uri, ip, created) VALUES (?, ?, ?, ?)",
                "ewm-main-service", "/events/2", "192.168.1.1", LocalDateTime.of(2023, 1, 2, 14, 0, 0)
        );
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWhereIpNotUnique() {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(List.of())
                .unique(false)
                .build();

        List<ViewStats> result = statsRepository.getStats(request);

        assertNotNull(result);
        assertEquals(2, result.size());
        for (ViewStats stat : result) {
            if (stat.getUri().equals("/events/1")) {
                assertEquals(2L, stat.getHits());
            } else if (stat.getUri().equals("/events/2")) {
                assertEquals(1L, stat.getHits());
            }
        }
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWithUniqueIp() {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(List.of())
                .unique(true)
                .build();

        List<ViewStats> result = statsRepository.getUniqueStats(request);

        assertNotNull(result);
        assertEquals(2, result.size());

        int totalHits = 0;
        for (ViewStats stat : result) {
            totalHits += stat.getHits();
            if (stat.getUri().equals("/events/1")) {
                assertEquals(2L, stat.getHits());
            } else if (stat.getUri().equals("/events/2")) {
                assertEquals(1L, stat.getHits());
            }
        }
        assertEquals(3L, totalHits);
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique() {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(false)
                .build();

        List<ViewStats> result = statsRepository.getStats(request);

        assertNotNull(result);
        assertEquals(2, result.size());

        for (ViewStats stat : result) {
            if (stat.getUri().equals("/events/1")) {
                assertEquals(2L, stat.getHits());
            } else if (stat.getUri().equals("/events/2")) {
                assertEquals(1L, stat.getHits());
            }
        }
    }

    @Test
    void findAllByTimestampBetweenStartAndEndWithUrisUniqueIp() {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(true)
                .build();

        List<ViewStats> result = statsRepository.getUniqueStats(request);

        assertNotNull(result);
        assertEquals(2, result.size());

        int totalHits = 0;
        for (ViewStats stat : result) {
            totalHits += stat.getHits();
            if (stat.getUri().equals("/events/1")) {
                assertEquals(2L, stat.getHits());
            } else if (stat.getUri().equals("/events/2")) {
                assertEquals(1L, stat.getHits());
            }
        }
        assertEquals(3L, totalHits);
    }
}