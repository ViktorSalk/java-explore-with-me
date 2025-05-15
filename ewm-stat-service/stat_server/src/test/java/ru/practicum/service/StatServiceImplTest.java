package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.exception.WrongTimeException;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    @Mock
    private StatServiceRepository statServiceRepository;

    @InjectMocks
    private StatServiceImpl statService;

    private StatDto statDto;
    private Stat stat;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        end = LocalDateTime.of(2023, 1, 2, 0, 0, 0);
        uris = List.of("/events/1", "/events/2");

        statDto = StatDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();

        stat = Stat.builder()
                .statId(1L)
                .app(statDto.getApp())
                .uri(statDto.getUri())
                .ip(statDto.getIp())
                .timestamp(statDto.getTimestamp())
                .build();
    }

    @Test
    void createStat_ShouldReturnStatDto() {
        when(statServiceRepository.save(any(Stat.class))).thenReturn(stat);

        StatDto result = statService.createStat(statDto);

        assertNotNull(result);
        assertEquals(statDto.getApp(), result.getApp());
        assertEquals(statDto.getUri(), result.getUri());
        assertEquals(statDto.getIp(), result.getIp());
        verify(statServiceRepository, times(1)).save(any(Stat.class));
    }

    @Test
    void readStat_WhenStartAfterEnd_ShouldThrowException() {
        LocalDateTime invalidStart = LocalDateTime.of(2023, 1, 3, 0, 0, 0);

        assertThrows(WrongTimeException.class, () ->
                statService.readStat(invalidStart, end, uris, false)
        );
    }

    @Test
    void readStat_WithEmptyUrisAndUniqueTrue_ShouldCallCorrectMethod() {
        List<StatResponseDto> expectedResponse = List.of(
                new StatResponseDto("ewm-main-service", "/events/1", 10)
        );
        when(statServiceRepository.findAllByTimestampBetweenStartAndEndWithUniqueIp(start, end))
                .thenReturn(expectedResponse);

        List<StatResponseDto> result = statService.readStat(start, end, List.of(), true);

        assertEquals(expectedResponse, result);
        verify(statServiceRepository).findAllByTimestampBetweenStartAndEndWithUniqueIp(start, end);
    }

    @Test
    void readStat_WithEmptyUrisAndUniqueFalse_ShouldCallCorrectMethod() {
        List<StatResponseDto> expectedResponse = List.of(
                new StatResponseDto("ewm-main-service", "/events/1", 15)
        );
        when(statServiceRepository.findAllByTimestampBetweenStartAndEndWhereIpNotUnique(start, end))
                .thenReturn(expectedResponse);

        List<StatResponseDto> result = statService.readStat(start, end, List.of(), false);

        assertEquals(expectedResponse, result);
        verify(statServiceRepository).findAllByTimestampBetweenStartAndEndWhereIpNotUnique(start, end);
    }

    @Test
    void readStat_WithUrisAndUniqueTrue_ShouldCallCorrectMethod() {
        List<StatResponseDto> expectedResponse = List.of(
                new StatResponseDto("ewm-main-service", "/events/1", 5)
        );
        when(statServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris))
                .thenReturn(expectedResponse);

        List<StatResponseDto> result = statService.readStat(start, end, uris, true);

        assertEquals(expectedResponse, result);
        verify(statServiceRepository).findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris);
    }

    @Test
    void readStat_WithUrisAndUniqueFalse_ShouldCallCorrectMethod() {
        List<StatResponseDto> expectedResponse = List.of(
                new StatResponseDto("ewm-main-service", "/events/1", 8)
        );
        when(statServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(start, end, uris))
                .thenReturn(expectedResponse);

        List<StatResponseDto> result = statService.readStat(start, end, uris, false);

        assertEquals(expectedResponse, result);
        verify(statServiceRepository).findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(start, end, uris);
    }
}