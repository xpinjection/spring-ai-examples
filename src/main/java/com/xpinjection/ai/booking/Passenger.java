package com.xpinjection.ai.booking;

import java.time.LocalDate;

public record Passenger(String name, String surname, LocalDate dateOfBirth) {
}
