package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.CreateTicketBlockRequest;
import com.example.ticketbooking.dto.TicketBlockResponse;

import java.util.List;

public interface TicketBlockService {
    TicketBlockResponse createBlock(CreateTicketBlockRequest request);

    List<TicketBlockResponse> getBlocksForEvent(Long eventId);
}
