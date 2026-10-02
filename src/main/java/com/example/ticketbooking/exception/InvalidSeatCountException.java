package com.example.ticketbooking.exception;

public class InvalidSeatCountException extends RuntimeException {
    public InvalidSeatCountException(Integer seatsRequested) {
        super("seatsRequested must be positive, got: " + seatsRequested);
    }
}
