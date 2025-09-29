package com.xpinjection.ai.booking;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryFlightRepository {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public InMemoryFlightRepository() {
        flights.put("XP100", new Flight("XP100", "Kyiv", "Lviv", 5, 49.99));
        flights.put("XP101", new Flight("XP101", "Kyiv", "Odessa", 3, 59.99));
        flights.put("XP200", new Flight("XP200", "Lviv", "Kyiv", 5, 39.99));
        flights.put("XP300", new Flight("XP300", "Berlin", "Paris", 10, 89.00));
        flights.put("XP301", new Flight("XP301", "Berlin", "Rome", 8, 99.00));
    }

    public List<Flight> listFlights(String departure, String destination) {
        return flights.values().stream()
                .filter(flight -> flight.departure().equalsIgnoreCase(departure))
                .filter(flight -> flight.destination().equalsIgnoreCase(destination))
                .map(flight -> new Flight(flight.flightNumber(), flight.departure(), flight.destination(),
                        remainingCapacity(flight), flight.ticketPrice()))
                .sorted(Comparator.comparing(Flight::flightNumber))
                .toList();
    }

    public Flight getFlight(String flightNumber) {
        return flights.get(flightNumber);
    }

    public String bookFlight(Flight flight, List<Passenger> passengers) {
        var bookingId = UUID.randomUUID().toString();
        var booking = new Booking(bookingId, flight.flightNumber(), passengers, BookingStatus.ACTIVE);
        bookings.put(bookingId, booking);
        return bookingId;
    }

    public int remainingCapacity(Flight flight) {
        int booked = bookings.values().stream()
                .filter(booking -> booking.flightNumber().equals(flight.flightNumber()))
                .filter(booking -> booking.status() == BookingStatus.ACTIVE)
                .mapToInt(booking -> booking.passengers().size())
                .sum();
        return Math.max(0, flight.capacity() - booked);
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    public void cancelBooking(Booking booking) {
        bookings.put(booking.bookingId(), new Booking(booking.bookingId(), booking.flightNumber(),
                booking.passengers(), BookingStatus.CANCELLED));
    }
}
