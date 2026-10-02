package com.example.ticketbooking.service.impl;

import com.example.ticketbooking.dto.CreateTicketBlockRequest;
import com.example.ticketbooking.dto.TicketBlockResponse;
import com.example.ticketbooking.entity.TicketBlock;
import com.example.ticketbooking.exception.EventNotFoundException;
import com.example.ticketbooking.repository.TicketBlockRepository;
import com.example.ticketbooking.service.TicketBlockService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketBlockServiceImpl implements TicketBlockService {

    private final TicketBlockRepository ticketBlockRepository;

    public TicketBlockServiceImpl(TicketBlockRepository ticketBlockRepository) {
        this.ticketBlockRepository = ticketBlockRepository;
    }

    @Override
    public TicketBlockResponse createBlock(CreateTicketBlockRequest request) {
        TicketBlock block = new TicketBlock(
                request.eventId(),
                request.blockName(),
                request.availableSeats(),
                request.saleEndDate());

        TicketBlock saved = ticketBlockRepository.save(block);
        return toResponse(saved);
    }

    @Override
    public List<TicketBlockResponse> getBlocksForEvent(Long eventId) {
        if (!ticketBlockRepository.existsByEventId(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        return ticketBlockRepository.findByEventIdOrderBySaleEndDateAsc(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketBlockResponse toResponse(TicketBlock block) {
        return new TicketBlockResponse(
                block.getId(),
                block.getBlockName(),
                block.getAvailableSeats(),
                block.getSaleEndDate());
    }
}
