package com.example.bookMyShow.model;

import com.example.bookMyShow.model.constant.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Ticket extends BaseModel{

    private double totalCost;
    @OneToMany
    private List<ShowSeat> showSeats;
    @ManyToOne
    private Showw show;
    @ManyToOne
    private User user;
    private LocalDateTime bookingTime;
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    // Add new fields
    @OneToOne
    private Payment payment; // Reference to Payment entity

    // Optional: If you need custom setter logic for cancelled
    // Optional: Customize getter for cancelled (using isCancelled() convention for booleans)

    private boolean cancelled; // Cancellation status, default to false

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public List<ShowSeat> getShowSeats() {
        return showSeats;
    }

    public void setShowSeats(List<ShowSeat> showSeats) {
        this.showSeats = showSeats;
    }

    public Showw getShow() {
        return show;
    }

    public void setShow(Showw show) {
        this.show = show;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
