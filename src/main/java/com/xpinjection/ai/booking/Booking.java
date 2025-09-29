package com.xpinjection.ai.booking;

import java.util.List;

public record Booking(String bookingId, String flightNumber,
                      List<Passenger> passengers, BookingStatus status) {
}
