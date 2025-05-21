package ru.practicum.ewm.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.ViewsStatsRequest;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private EndpointHit endpointHit;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        end = LocalDateTime.of(2023, 1, 2, 0, 0, 0);
        uris = List.of("/events/1", "/events/2");

        endpointHit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void saveHit_ShouldCallRepositorySaveHit() {
        doNothing().when(statsRepository).saveHit(any(EndpointHit.class));

        statsService.saveHit(endpointHit);

        verify(statsRepository, times(1)).saveHit(any(EndpointHit.class));
    }

    @Test
    void getViewStatsList_WhenStartAfterEnd_ShouldNotThrowException() {
        LocalDateTime invalidStart = LocalDateTime.of(2023, 1, 3, 0, 0, 0);
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(invalidStart)
                .end(end)
                .uris(uris)
                .unique(false)
                .build();

        List<ViewStats> expectedResponse = List.of();
        when(statsRepository.getStats(any(ViewsStatsRequest.class)))
                .thenReturn(expectedResponse);

        List<ViewStats> result = statsService.getViewStatsList(request);

        verify(statsRepository).getStats(any(ViewsStatsRequest.class));
    }


    @Test
    void getViewStatsList_WithEmptyUrisAndUniqueTrue_ShouldCallGetUniqueStats() {
        List<ViewStats> expectedResponse = List.of(
                ViewStats.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(10L)
                        .build()
        );

        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(List.of())
                .unique(true)
                .build();

        when(statsRepository.getUniqueStats(any(ViewsStatsRequest.class)))
                .thenReturn(expectedResponse);

        List<ViewStats> result = statsService.getViewStatsList(request);

        assertEquals(expectedResponse, result);
        verify(statsRepository).getUniqueStats(any(ViewsStatsRequest.class));
    }

    @Test
    void getViewStatsList_WithEmptyUrisAndUniqueFalse_ShouldCallGetStats() {
        List<ViewStats> expectedResponse = List.of(
                ViewStats.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(15L)
                        .build()
        );

        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(List.of())
                .unique(false)
                .build();

        when(statsRepository.getStats(any(ViewsStatsRequest.class)))
                .thenReturn(expectedResponse);

        List<ViewStats> result = statsService.getViewStatsList(request);

        assertEquals(expectedResponse, result);
        verify(statsRepository).getStats(any(ViewsStatsRequest.class));
    }

    @Test
    void getViewStatsList_WithUrisAndUniqueTrue_ShouldCallGetUniqueStats() {
        List<ViewStats> expectedResponse = List.of(
                ViewStats.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(5L)
                        .build()
        );

        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(true)
                .build();

        when(statsRepository.getUniqueStats(any(ViewsStatsRequest.class)))
                .thenReturn(expectedResponse);

        List<ViewStats> result = statsService.getViewStatsList(request);

        assertEquals(expectedResponse, result);
        verify(statsRepository).getUniqueStats(any(ViewsStatsRequest.class));
    }

    @Test
    void getViewStatsList_WithUrisAndUniqueFalse_ShouldCallGetStats() {
        List<ViewStats> expectedResponse = List.of(
                ViewStats.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(8L)
                        .build()
        );

        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(false)
                .build();

        when(statsRepository.getStats(any(ViewsStatsRequest.class)))
                .thenReturn(expectedResponse);

        List<ViewStats> result = statsService.getViewStatsList(request);

        assertEquals(expectedResponse, result);
        verify(statsRepository).getStats(any(ViewsStatsRequest.class));
    }
}