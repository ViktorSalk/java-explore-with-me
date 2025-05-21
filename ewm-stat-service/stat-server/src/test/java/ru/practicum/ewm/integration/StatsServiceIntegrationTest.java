package ru.practicum.ewm.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatsServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        jdbcTemplate.update("DELETE FROM stats");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM stats");
    }

    @Test
    void saveAndRetrieveStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);

        EndpointHit hit1 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        EndpointHit hit2 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.2")
                .timestamp(now)
                .build();

        EndpointHit hit3 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHit> request1 = new HttpEntity<>(hit1, headers);
        HttpEntity<EndpointHit> request2 = new HttpEntity<>(hit2, headers);
        HttpEntity<EndpointHit> request3 = new HttpEntity<>(hit3, headers);

        ResponseEntity<Void> response1 = restTemplate.postForEntity(baseUrl + "/hit", request1, Void.class);
        ResponseEntity<Void> response2 = restTemplate.postForEntity(baseUrl + "/hit", request2, Void.class);
        ResponseEntity<Void> response3 = restTemplate.postForEntity(baseUrl + "/hit", request3, Void.class);

        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertEquals(HttpStatus.CREATED, response3.getStatusCode());

        String getUrl = baseUrl + "/stats?start={start}&end={end}&unique=false";
        ResponseEntity<ViewStats[]> getResponse = restTemplate.getForEntity(
                getUrl,
                ViewStats[].class,
                start.format(formatter),
                end.format(formatter)
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        ViewStats[] stats = getResponse.getBody();
        assertEquals(2, stats.length);

        List<ViewStats> statsList = Arrays.asList(stats);
        for (ViewStats stat : statsList) {
            if (stat.getUri().equals("/events/1")) {
                assertEquals(2L, stat.getHits());
            } else if (stat.getUri().equals("/events/2")) {
                assertEquals(1L, stat.getHits());
            }
        }

        String getUniqueUrl = baseUrl + "/stats?start={start}&end={end}&unique=true";
        ResponseEntity<ViewStats[]> getUniqueResponse = restTemplate.getForEntity(
                getUniqueUrl,
                ViewStats[].class,
                start.format(formatter),
                end.format(formatter)
        );

        assertEquals(HttpStatus.OK, getUniqueResponse.getStatusCode());
        assertNotNull(getUniqueResponse.getBody());
        ViewStats[] uniqueStats = getUniqueResponse.getBody();
        assertEquals(2, uniqueStats.length);

        List<ViewStats> uniqueStatsList = Arrays.asList(uniqueStats);
        for (ViewStats stat : uniqueStatsList) {
            if (stat.getUri().equals("/events/1")) {
                assertEquals(2L, stat.getHits());
            } else if (stat.getUri().equals("/events/2")) {
                assertEquals(1L, stat.getHits());
            }
        }
    }

    @Test
    void getStatsWithUriFilter() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);

        EndpointHit hit1 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        EndpointHit hit2 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHit> request1 = new HttpEntity<>(hit1, headers);
        HttpEntity<EndpointHit> request2 = new HttpEntity<>(hit2, headers);

        restTemplate.postForEntity(baseUrl + "/hit", request1, Void.class);
        restTemplate.postForEntity(baseUrl + "/hit", request2, Void.class);

        String getUrl = baseUrl + "/stats?start={start}&end={end}&uris={uris}&unique=false";
        ResponseEntity<ViewStats[]> getResponse = restTemplate.getForEntity(
                getUrl,
                ViewStats[].class,
                start.format(formatter),
                end.format(formatter),
                "/events/1"
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        ViewStats[] stats = getResponse.getBody();
        assertEquals(1, stats.length);
        assertEquals("/events/1", stats[0].getUri());
        assertEquals(1L, stats[0].getHits());
    }

    @Test
    void handleInvalidDateRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.minusDays(1);

        String getUrl = baseUrl + "/stats?start={start}&end={end}&unique=false";
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                getUrl,
                String.class,
                start.format(formatter),
                end.format(formatter)
        );

        assertEquals(HttpStatus.BAD_REQUEST, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertTrue(getResponse.getBody().contains("Uncorrected format of dates"));
    }
}