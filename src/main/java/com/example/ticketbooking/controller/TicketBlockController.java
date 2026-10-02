package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.CreateTicketBlockRequest;
import com.example.ticketbooking.dto.TicketBlockResponse;
import com.example.ticketbooking.service.TicketBlockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket-blocks")
public class TicketBlockController {

    private final TicketBlockService ticketBlockService;

    public TicketBlockController(TicketBlockService ticketBlockService) {
        this.ticketBlockService = ticketBlockService;
    }

    @PostMapping
    public ResponseEntity<TicketBlockResponse> create(@Valid @RequestBody CreateTicketBlockRequest request) {
        TicketBlockResponse response = ticketBlockService.createBlock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<TicketBlockResponse>> getBlocks(@PathVariable Long eventId) {
        return ResponseEntity.ok(ticketBlockService.getBlocksForEvent(eventId));
    }
}
