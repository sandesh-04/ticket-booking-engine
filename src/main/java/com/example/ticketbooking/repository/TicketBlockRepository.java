package com.example.ticketbooking.repository;

import com.example.ticketbooking.entity.TicketBlock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TicketBlockRepository extends JpaRepository<TicketBlock, Long> {

    /**
     * Locks every usable block for this event (not sold out, sale window not yet
     * closed) so that two concurrent booking requests for the same event cannot
     * both read-and-overwrite the same seat counts. Ordered so the block whose
     * sale window closes soonest is drawn from first.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b FROM TicketBlock b
            WHERE b.eventId = :eventId
                AND b.availableSeats > 0
                AND b.saleEndDate >= :today
            ORDER BY b.saleEndDate ASC
            """)
    List<TicketBlock> findUsableBlocksForUpdate(
            @Param("eventId") Long eventId,
            @Param("today") LocalDate today);

    List<TicketBlock> findByEventIdOrderBySaleEndDateAsc(Long eventId);

    boolean existsByEventId(Long eventId);
}
