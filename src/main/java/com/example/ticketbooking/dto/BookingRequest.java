package com.example.ticketbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull(message = "eventId is required")
        Long eventId,

        @NotNull(message = "seatsRequested is required")
        @Min(value = 1, message = "seatsRequested must be at least 1")
        Integer seatsRequested
) {
}
