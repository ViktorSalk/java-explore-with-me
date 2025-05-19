package ru.practicum.ewm.controller.pub.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.SearchEventParams;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.model.constants.EventStatus;
import ru.practicum.ewm.service.event.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventPublicController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EventPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(1);

        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        UserShortDto userShortDto = UserShortDto.builder()
                .id(1L)
                .name("Test User")
                .build();

        eventShortDto = EventShortDto.builder()
                .id(1L)
                .annotation("Test Annotation")
                .category(categoryDto)
                .confirmedRequests(0)
                .eventDate(eventDate)
                .initiator(userShortDto)
                .paid(false)
                .title("Test Event")
                .views(0L)
                .build();

        eventFullDto = EventFullDto.builder()
                .id(1L)
                .annotation("Test Annotation")
                .category(categoryDto)
                .confirmedRequests(0)
                .createdOn(LocalDateTime.now().minusDays(1))
                .description("Test Description")
                .eventDate(eventDate)
                .initiator(userShortDto)
                .paid(false)
                .participantLimit(10)
                .publishedOn(LocalDateTime.now())
                .requestModeration(true)
                .state(EventStatus.PUBLISHED)
                .title("Test Event")
                .views(0L)
                .build();
    }

    @Test
    void getAllEvents_WithMinimalParams_ShouldReturnOkStatus() throws Exception {
        when(eventService.getAllEventFromPublic(any(SearchEventParams.class), any())).thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventShortDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.getTitle()));
    }

    @Test
    void getEventById_ShouldReturnOkStatus() throws Exception {
        when(eventService.getEventById(anyLong(), any())).thenReturn(eventFullDto);

        mockMvc.perform(get("/events/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$.description").value(eventFullDto.getDescription()));
    }
}