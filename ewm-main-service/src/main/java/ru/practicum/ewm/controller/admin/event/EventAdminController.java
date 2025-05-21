package ru.practicum.ewm.controller.admin.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.SearchEventParamsAdmin;
import ru.practicum.ewm.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEvents(@Valid SearchEventParamsAdmin searchEventParamsAdmin) {
        log.info("GET запрос на получение списка событий");
        return eventService.getAllEventFromAdmin(searchEventParamsAdmin);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable(value = "eventId") @Min(1) Long eventId,
                                           @RequestBody @Valid UpdateEventAdminRequest inputUpdate) {
        log.info("PATCH запрос на обновление списка событий");
        return eventService.updateEventFromAdmin(eventId, inputUpdate);
    }
}