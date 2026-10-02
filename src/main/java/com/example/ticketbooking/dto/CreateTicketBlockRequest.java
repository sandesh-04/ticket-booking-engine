package com.example.ticketbooking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTicketBlockRequest(
        @NotNull(message = "eventId is required")
        Long eventId,

        @NotBlank(message = "blockName is required")
        String blockName,

        @NotNull(message = "availableSeats is required")
        @Min(value = 1, message = "availableSeats must be at least 1")
        Integer availableSeats,

        @NotNull(message = "saleEndDate is required")
        @FutureOrPresent(message = "saleEndDate must be today or later")
        LocalDate saleEndDate
) {
}
