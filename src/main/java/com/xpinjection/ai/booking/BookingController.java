package com.xpinjection.ai.booking;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/booking", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping(path = "/assist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AssistResponse assist(@RequestBody AssistRequest request) {
        var reply = bookingService.assist(request.sessionId(), request.message());
        return new AssistResponse(reply);
    }

    public record AssistRequest(String sessionId, String message) {}
    public record AssistResponse(String reply) {}
}
