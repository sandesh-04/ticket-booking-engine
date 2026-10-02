package com.example.ticketbooking.service.impl;

import com.example.ticketbooking.dto.BlockAllocation;
import com.example.ticketbooking.dto.BookingResponse;
import com.example.ticketbooking.entity.TicketBlock;
import com.example.ticketbooking.exception.EventNotFoundException;
import com.example.ticketbooking.exception.InvalidSeatCountException;
import com.example.ticketbooking.repository.TicketBlockRepository;
import com.example.ticketbooking.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final TicketBlockRepository ticketBlockRepository;

    public BookingServiceImpl(TicketBlockRepository ticketBlockRepository) {
        this.ticketBlockRepository = ticketBlockRepository;
    }

    @Override
    @Transactional
    public BookingResponse book(Long eventId, Integer seatsRequested) {
        if (seatsRequested == null || seatsRequested <= 0) {
            throw new InvalidSeatCountException(seatsRequested);
        }

        if (!ticketBlockRepository.existsByEventId(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        // Pessimistic write lock: while this transaction holds these rows, no
        // other concurrent booking for this event can read-then-overwrite the
        // same seat counts, which is what prevents overselling.
        List<TicketBlock> blocks = ticketBlockRepository.findUsableBlocksForUpdate(eventId, LocalDate.now());

        List<BlockAllocation> taken = new ArrayList<>();
        int remaining = seatsRequested;

        for (TicketBlock block : blocks) {
            if (remaining == 0) {
                break;
            }

            int draw = Math.min(block.getAvailableSeats(), remaining);

            block.setAvailableSeats(block.getAvailableSeats() - draw);
            taken.add(new BlockAllocation(block.getId(), draw));

            remaining -= draw;
        }

        if (remaining > 0) {
            // Not enough seats across every usable block for this event.
            // Since no exception is thrown, this @Transactional method will
            // still commit normally - so every block touched above must be
            // manually reverted to its original seat count before returning,
            // otherwise the partial draw would silently persist even though
            // the booking as a whole "failed".
            restore(blocks, taken);
            return BookingResponse.failure(
                    "Not enough seats available. No seats were booked; all block availability remains unchanged.");
        }

        return BookingResponse.success(taken);
    }

    private void restore(List<TicketBlock> blocks, List<BlockAllocation> taken) {
        Map<Long, TicketBlock> byId = blocks.stream()
                .collect(Collectors.toMap(TicketBlock::getId, b -> b));

        for (BlockAllocation allocation : taken) {
            TicketBlock block = byId.get(allocation.blockId());
            if (block != null) {
                block.setAvailableSeats(block.getAvailableSeats() + allocation.seatsTaken());
            }
        }
    }
}
