package com.example.ticketbooking.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A block of seats for an event (e.g. "Early Bird", "Regular", "VIP"),
 * each with its own remaining seat count and its own sale-end date.
 * An event's total availability is the sum of its blocks, not a single number.
 */
@Entity
public class TicketBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private String blockName;

    private Integer availableSeats;

    private LocalDate saleEndDate;

    @Version
    private Long version;

    protected TicketBlock() {
    }

    public TicketBlock(Long eventId, String blockName, Integer availableSeats, LocalDate saleEndDate) {
        this.eventId = eventId;
        this.blockName = blockName;
        this.availableSeats = availableSeats;
        this.saleEndDate = saleEndDate;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getBlockName() {
        return blockName;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public LocalDate getSaleEndDate() {
        return saleEndDate;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketBlock that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
