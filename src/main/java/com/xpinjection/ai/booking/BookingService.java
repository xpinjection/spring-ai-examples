package com.xpinjection.ai.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
    private static final Logger LOG = LoggerFactory.getLogger(BookingService.class);

    private static final String SYSTEM_PROMPT = "You are a helpful flight booking assistant. " +
            "Use the available tools to: 1) list flights by departure and destination, " +
            "2) book flight tickets for provided passengers, 3) cancel existing bookings. " +
            "Be concise and helpful. If a tool returns structured data, summarize it for the user.";


    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final InMemoryFlightRepository repository;

    public BookingService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, InMemoryFlightRepository repository) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.chatMemory = chatMemory;
        this.repository = repository;
    }

    public String assist(String sessionId, String userMessage) {
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(sessionId)
                .build();

        return chatClient
                .prompt()
                .advisors(memoryAdvisor)
                .tools(this)
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();
    }

    @Tool(name = "listFlights", description = "List available flights between departure and destination cities")
    public List<Flight> listFlights(
            @ToolParam(description = "Departure city name") String departure,
            @ToolParam(description = "Destination city name") String destination) {
        LOG.info("listFlights called: departure={}, destination={}", departure, destination);
        var flights = repository.listFlights(departure, destination);
        LOG.info("listFlights result: {}", flights);
        return flights;
    }

    @Tool(name = "bookFlight", description = "Book a flight passing a flightNumber and a list of passengers (name, surname, dateOfBirth). Returns bookingId")
    public BookFlightResult bookFlight(
            @ToolParam(description = "Flight number identifier (e.g., XP100)") String flightNumber,
            @ToolParam(description = "List of passengers (name, surname, dateOfBirth) to book") List<Passenger> passengers) {
        LOG.info("bookFlight called: flightNumber={}, passengers={}", flightNumber, passengers);

        var flight = repository.getFlight(flightNumber);
        if (flight == null) {
            LOG.warn("bookFlight: flight not found: {}", flightNumber);
            return new BookFlightResult(null, false, "Flight not found: " + flightNumber);
        }
        if (passengers == null || passengers.isEmpty()) {
            LOG.warn("bookFlight: no passengers provided");
            return new BookFlightResult(null, false, "At least one passenger is required");
        }
        var remainingCapacity = repository.remainingCapacity(flight);
        if (passengers.size() > remainingCapacity) {
            LOG.warn("bookFlight: capacity exceeded for flight {}, requested={}, remaining={}", flightNumber, passengers.size(), remainingCapacity);
            return new BookFlightResult(null, false, "Not enough seats. Remaining: " + remainingCapacity);
        }
        var bookingId = repository.bookFlight(flight, passengers);
        LOG.info("bookFlight result: {}", bookingId);
        return new BookFlightResult(bookingId, true, "Booked successfully");
    }

    @Tool(name = "cancelBooking", description = "Cancel a booking by its bookingId. Cancelled bookings do not restore consumed capacity for past bookings.")
    public CancelResult cancelBooking(@ToolParam(description = "Booking identifier returned by bookFlight") String bookingId) {
        LOG.info("cancelBooking called: bookingId={}", bookingId);
        var booking = repository.getBooking(bookingId);
        if (booking == null) {
            LOG.warn("cancelBooking: booking not found: {}", bookingId);
            return new CancelResult(false, "Booking not found: " + bookingId);
        }
        if (booking.status() == BookingStatus.CANCELLED) {
            LOG.info("cancelBooking: already cancelled: {}", bookingId);
            return new CancelResult(true, "Already cancelled");
        }
        repository.cancelBooking(booking);
        LOG.info("cancelBooking result: success");
        return new CancelResult(true, "Cancelled successfully");
    }

    private record BookFlightResult(String bookingId, boolean success, String message) {}
    private record CancelResult(boolean success, String message) {}
}
