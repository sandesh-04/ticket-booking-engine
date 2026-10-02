package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.BookingRequest;
import com.example.ticketbooking.dto.BookingResponse;
import com.example.ticketbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.book(request.eventId(), request.seatsRequested());
        HttpStatus status = response.success() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY;

        return ResponseEntity.status(status).body(response);
    }
}
