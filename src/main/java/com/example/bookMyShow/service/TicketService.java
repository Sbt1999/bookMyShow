package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.PaymentProcessingException;
import com.example.bookMyShow.exception.SelectedSeatsNotAvailableException;
import com.example.bookMyShow.model.Payment;
import com.example.bookMyShow.model.ShowSeat;
import com.example.bookMyShow.model.Ticket;
import com.example.bookMyShow.model.User;
import com.example.bookMyShow.model.constant.PaymentMethod;
import com.example.bookMyShow.model.constant.ShowSeatStatus;
import com.example.bookMyShow.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/*
SpringBoot have very interesting concept IOC and DI
Inversion of Control -> Means giving control to SpringBoot to control the DI
 */

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ShowSeatService showSeatService;
    @Autowired
    private ShowService showService;
    @Autowired
    private PaymentService paymentService;

    /**
     * Create a ticket with payment processing
     * @param userId User ID
     * @param showSeatIds List of show seat IDs
     * @param paymentMethod Payment method
     * @return Created ticket
     */
    @Transactional
    public Ticket createTicket(int userId, List<Integer> showSeatIds, PaymentMethod paymentMethod)
            throws SelectedSeatsNotAvailableException, PaymentProcessingException {

        User user = userService.getUserById(userId);
        List<ShowSeat> showSeats;
        double totalCost = 0;

        // Step 1: Check and lock seats
        showSeats = checkAndLockShowSeats(showSeatIds);

        // Step 2: Calculate total cost
        for (ShowSeat showSeat : showSeats) {
            totalCost += showSeat.getPrice();
        }

        // Step 3: Apply taxes and fees
        totalCost = paymentService.calculateTotalAmount(totalCost);

        // Step 4: Create ticket (initially without payment)
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setShowSeats(showSeats);
        ticket.setShow(showSeats.get(0).getShow());
        ticket.setTotalCost(totalCost);
        ticket = ticketRepository.save(ticket);

        try {
            // Step 5: Process payment
            Payment payment = paymentService.processPayment(user, ticket, paymentMethod, totalCost);

            // Step 6: If payment successful, confirm booking
            if (payment.getPaymentStatus().name().equals("SUCCESS")) {
                // Mark seats as booked
                for (ShowSeat showSeat : showSeats) {
                    showSeat.setShowSeatStatus(ShowSeatStatus.BOOKED);
                    showSeatService.updateShowSeat(showSeat);
                }

                // Update ticket with payment reference
                ticket.setPayment(payment);
                ticket = ticketRepository.save(ticket);

                return ticket;
            } else {
                // Payment failed, release seats and delete ticket
                releaseSeatLocks(showSeats);
                ticketRepository.delete(ticket);
                throw new PaymentProcessingException("Payment failed: " + payment.getGatewayResponse());
            }

        } catch (PaymentProcessingException e) {
            // Payment processing failed, release seats and delete ticket
            releaseSeatLocks(showSeats);
            ticketRepository.delete(ticket);
            throw e;
        } catch (Exception e) {
            // Any other exception, release seats and delete ticket
            releaseSeatLocks(showSeats);
            ticketRepository.delete(ticket);
            throw new PaymentProcessingException("Ticket creation failed: " + e.getMessage());
        }
    }

    /**
     * Cancel a ticket and process refund
     * @param ticketId Ticket ID
     * @return Refund payment
     */
    @Transactional
    public Payment cancelTicket(int ticketId) throws PaymentProcessingException {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Check if ticket can be cancelled (e.g., show hasn't started)
        if (!canCancelTicket(ticket)) {
            throw new PaymentProcessingException("Ticket cannot be cancelled");
        }

        // Calculate refund amount (may include cancellation charges)
        double refundAmount = calculateRefundAmount(ticket);

        // Process refund
        Payment refund = paymentService.processRefund(ticket.getPayment(), refundAmount);

        // Release seats
        for (ShowSeat showSeat : ticket.getShowSeats()) {
            showSeat.setShowSeatStatus(ShowSeatStatus.AVAILABLE);
            showSeatService.updateShowSeat(showSeat);
        }

        // Mark ticket as cancelled
        ticket.setCancelled(true);
        ticketRepository.save(ticket);

        return refund;
    }

    /**
     * Check and lock show seats with proper concurrency control
     * @param showSeatIds List of show seat IDs
     * @return List of locked show seats
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ShowSeat> checkAndLockShowSeats(List<Integer> showSeatIds)
            throws SelectedSeatsNotAvailableException {

        List<ShowSeat> showSeats = new ArrayList<>();

        // Step 1: Check if all seats are available
        for (int showSeatId : showSeatIds) {
            ShowSeat showSeat = showSeatService.getShowSeatById(showSeatId);
            if (!showSeat.getShowSeatStatus().equals(ShowSeatStatus.AVAILABLE)) {
                throw new SelectedSeatsNotAvailableException(
                        "Seat " + showSeat.getSeat().getSeatNumber() + " is not available");
            }
            showSeats.add(showSeat);
        }

        // Step 2: Lock all seats (separate loop to avoid partial locks)
        for (ShowSeat showSeat : showSeats) {
            showSeat.setShowSeatStatus(ShowSeatStatus.LOCKED);
            showSeatService.updateShowSeat(showSeat);
        }

        return showSeats;
    }

    /**
     * Release seat locks (used when payment fails)
     * @param showSeats List of show seats to unlock
     */
    private void releaseSeatLocks(List<ShowSeat> showSeats) {
        for (ShowSeat showSeat : showSeats) {
            showSeat.setShowSeatStatus(ShowSeatStatus.AVAILABLE);
            showSeatService.updateShowSeat(showSeat);
        }
    }

    /**
     * Check if ticket can be cancelled based on business rules
     * @param ticket Ticket to check
     * @return true if cancellable
     */
    private boolean canCancelTicket(Ticket ticket) {
        // Example business rule: Can cancel up to 2 hours before show time
        return ticket.getShow().getStartTime().minusHours(2).isAfter(java.time.LocalDateTime.now());
    }

    /**
     * Calculate refund amount based on cancellation policy
     * @param ticket Ticket to calculate refund for
     * @return Refund amount
     */
    private double calculateRefundAmount(Ticket ticket) {
        double totalCost = ticket.getTotalCost();

        // Example cancellation policy:
        // - Cancel >24 hours before: 90% refund
        // - Cancel 2-24 hours before: 50% refund
        // - Cancel <2 hours before: No refund

        long hoursBeforeShow = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                ticket.getShow().getStartTime()
        ).toHours();

        if (hoursBeforeShow > 24) {
            return totalCost * 0.9; // 90% refund
        } else if (hoursBeforeShow > 2) {
            return totalCost * 0.5; // 50% refund
        } else {
            return 0; // No refund
        }
    }

    /**
     * Get ticket by ID
     * @param id Ticket ID
     * @return Ticket
     */
    public Ticket getTicketById(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    /**
     * Get all tickets for a user
     * @param userId User ID
     * @return List of tickets
     */
    public List<Ticket> getTicketsByUserId(int userId) {
        return ticketRepository.findByUserId(userId);
    }

    /**
     * Get all tickets
     * @return List of all tickets
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }
}
//
//package com.example.bookMyShow.service;
//
//import com.example.bookMyShow.exception.PaymentProcessingException;
//import com.example.bookMyShow.exception.SelectedSeatsNotAvailableException;
//import com.example.bookMyShow.model.Payment;
//import com.example.bookMyShow.model.ShowSeat;
//import com.example.bookMyShow.model.Ticket;
//import com.example.bookMyShow.model.User;
//import com.example.bookMyShow.model.constant.PaymentMethod;
//import com.example.bookMyShow.model.constant.ShowSeatStatus;
//import com.example.bookMyShow.repository.TicketRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Isolation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class TicketService {
//
//    @Autowired
//    private TicketRepository ticketRepository;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private ShowSeatService showSeatService;
//    @Autowired
//    private ShowService showService;
//    @Autowired
//    private PaymentService paymentService;
//
//    /**
//     * Create a ticket with payment processing
//     * @param userId User ID
//     * @param showSeatIds List of show seat IDs
//     * @param paymentMethod Payment method
//     * @return Created ticket
//     */
//    @Transactional
//    public Ticket createTicket(int userId, List<Integer> showSeatIds, PaymentMethod paymentMethod)
//            throws SelectedSeatsNotAvailableException, PaymentProcessingException {
//
//        User user = userService.getUserById(userId);
//        List<ShowSeat> showSeats;
//        double totalCost = 0;
//
//        // Step 1: Check and lock seats
//        showSeats = checkAndLockShowSeats(showSeatIds);
//
//        // Step 2: Calculate total cost
//        for (ShowSeat showSeat : showSeats) {
//            totalCost += showSeat.getPrice();
//        }
//
//        // Step 3: Apply taxes and fees
//        totalCost = paymentService.calculateTotalAmount(totalCost);
//
//        // Step 4: Create ticket (initially without payment)
//        Ticket ticket = new Ticket();
//        ticket.setUser(user);
//        ticket.setShowSeats(showSeats);
//        ticket.setShow(showSeats.get(0).getShow());
//        ticket.setTotalCost(totalCost);
//        ticket = ticketRepository.save(ticket);
//
//        try {
//            // Step 5: Process payment
//            Payment payment = paymentService.processPayment(user, ticket, paymentMethod, totalCost);
//
//            // Step 6: If payment successful, confirm booking
//            if (payment.getPaymentStatus().name().equals("SUCCESS")) {
//                // Mark seats as booked
//                for (ShowSeat showSeat : showSeats) {
//                    showSeat.setShowSeatStatus(ShowSeatStatus.BOOKED);
//                    showSeatService.updateShowSeat(showSeat);
//                }
//
//                // Update ticket with payment reference
//                ticket.setPayment(payment);
//                ticket = ticketRepository.save(ticket);
//
//                return ticket;
//            } else {
//                // Payment failed, release seats and delete ticket
//                releaseSeatLocks(showSeats);
//                ticketRepository.delete(ticket);
//                throw new PaymentProcessingException("Payment failed: " + payment.getGatewayResponse());
//            }
//
//        } catch (PaymentProcessingException e) {
//            // Payment processing failed, release seats and delete ticket
//            releaseSeatLocks(showSeats);
//            ticketRepository.delete(ticket);
//            throw e;
//        } catch (Exception e) {
//            // Any other exception, release seats and delete ticket
//            releaseSeatLocks(showSeats);
//            ticketRepository.delete(ticket);
//            throw new PaymentProcessingException("Ticket creation failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Cancel a ticket and process refund
//     * @param ticketId Ticket ID
//     * @return Refund payment
//     */
//    @Transactional
//    public Payment cancelTicket(int ticketId) throws PaymentProcessingException {
//        Ticket ticket = ticketRepository.findById(ticketId)
//                .orElseThrow(() -> new RuntimeException("Ticket not found"));
//
//        // Check if ticket can be cancelled (e.g., show hasn't started)
//        if (!canCancelTicket(ticket)) {
//            throw new PaymentProcessingException("Ticket cannot be cancelled");
//        }
//
//        // Calculate refund amount (may include cancellation charges)
//        double refundAmount = calculateRefundAmount(ticket);
//
//        // Process refund
//        Payment refund = paymentService.processRefund(ticket.getPayment(), refundAmount);
//
//        // Release seats
//        for (ShowSeat showSeat : ticket.getShowSeats()) {
//            showSeat.setShowSeatStatus(ShowSeatStatus.AVAILABLE);
//            showSeatService.updateShowSeat(showSeat);
//        }
//
//        // Mark ticket as cancelled
//        ticket.setCancelled(true);
//        ticketRepository.save(ticket);
//
//        return refund;
//    }
//
//    /**
//     * Check and lock show seats with proper concurrency control
//     * @param showSeatIds List of show seat IDs
//     * @return List of locked show seats
//     */
//    @Transactional(isolation = Isolation.SERIALIZABLE)
//    public List<ShowSeat> checkAndLockShowSeats(List<Integer> showSeatIds)
//            throws SelectedSeatsNotAvailableException {
//
//        List<ShowSeat> showSeats = new ArrayList<>();
//
//        // Step 1: Check if all seats are available
//        for (int showSeatId : showSeatIds) {
//            ShowSeat showSeat = showSeatService.getShowSeatById(showSeatId);
//            if (!showSeat.getShowSeatStatus().equals(ShowSeatStatus.AVAILABLE)) {
//                throw new SelectedSeatsNotAvailableException(
//                        "Seat " + showSeat.getSeat().getSeatNumber() + " is not available");
//            }
//            showSeats.add(showSeat);
//        }
//
//        // Step 2: Lock all seats (separate loop to avoid partial locks)
//        for (ShowSeat showSeat : showSeats) {
//            showSeat.setShowSeatStatus(ShowSeatStatus.LOCKED);
//            showSeatService.updateShowSeat(showSeat);
//        }
//
//        return showSeats;
//    }
//
//    /**
//     * Release seat locks (used when payment fails)
//     * @param showSeats List of show seats to unlock
//     */
//    private void releaseSeatLocks(List<ShowSeat> showSeats) {
//        for (ShowSeat showSeat : showSeats) {
//            showSeat.setShowSeatStatus(ShowSeatStatus.AVAILABLE);
//            showSeatService.updateShowSeat(showSeat);
//        }
//    }
//
//    /**
//     * Check if ticket can be cancelled based on business rules
//     * @param ticket Ticket to check
//     * @return true if cancellable
//     */
//    private boolean canCancelTicket(Ticket ticket) {
//        // Example business rule: Can cancel up to 2 hours before show time
//        return ticket.getShow().getStartTime().minusHours(2).isAfter(java.time.LocalDateTime.now());
//    }
//
//    /**
//     * Calculate refund amount based on cancellation policy
//     * @param ticket Ticket to calculate refund for
//     * @return Refund amount
//     */
//    private double calculateRefundAmount(Ticket ticket) {
//        double totalCost = ticket.getTotalCost();
//
//        // Example cancellation policy:
//        // - Cancel >24 hours before: 90% refund
//        // - Cancel 2-24 hours before: 50% refund
//        // - Cancel <2 hours before: No refund
//
//        long hoursBeforeShow = java.time.Duration.between(
//                java.time.LocalDateTime.now(),
//                ticket.getShow().getStartTime()
//        ).toHours();
//
//        if (hoursBeforeShow > 24) {
//            return totalCost * 0.9; // 90% refund
//        } else if (hoursBeforeShow > 2) {
//            return totalCost * 0.5; // 50% refund
//        } else {
//            return 0; // No refund
//        }
//    }
//
//    /**
//     * Get ticket by ID
//     * @param id Ticket ID
//     * @return Ticket
//     */
//    public Ticket getTicketById(int id) {
//        return ticketRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Ticket not found"));
//    }
//
//    /**
//     * Get all tickets for a user
//     * @param userId User ID
//     * @return List of tickets
//     */
//    public List<Ticket> getTicketsByUserId(int userId) {
//        return ticketRepository.findByUserId(userId);
//    }
//
//    /**
//     * Get all tickets
//     * @return List of all tickets
//     */
//    public List<Ticket> getAllTickets() {
//        return ticketRepository.findAll();
//    }
//}
