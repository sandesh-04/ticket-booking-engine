package com.example.ticketbooking.dto;

import java.time.LocalDate;

public record TicketBlockResponse(
        Long blockId,
        String blockName,
        Integer availableSeats,
        LocalDate saleEndDate
) {
}
