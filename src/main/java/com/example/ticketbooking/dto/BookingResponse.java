package com.example.ticketbooking.dto;

import java.util.List;

public record BookingResponse(
        boolean success,
        String message,
        List<BlockAllocation> breakdown
) {
    public static BookingResponse success(List<BlockAllocation> breakdown) {
        return new BookingResponse(true, "Booking successful", breakdown);
    }

    public static BookingResponse failure(String message) {
        return new BookingResponse(false, message, List.of());
    }
}
