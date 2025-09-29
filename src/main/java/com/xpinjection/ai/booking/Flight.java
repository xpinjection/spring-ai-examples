package com.xpinjection.ai.booking;

public record Flight(String flightNumber, String departure, String destination,
                     int capacity, double ticketPrice) {
}
