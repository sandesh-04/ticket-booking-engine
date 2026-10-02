package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.BookingResponse;

public interface BookingService {
    BookingResponse book(Long eventId, Integer seatsRequested);
}
