package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.PaymentNotFoundException;
import com.example.bookMyShow.exception.PaymentProcessingException;
import com.example.bookMyShow.model.Payment;
import com.example.bookMyShow.model.Ticket;
import com.example.bookMyShow.model.User;
import com.example.bookMyShow.model.constant.PaymentMethod;
import com.example.bookMyShow.model.constant.PaymentStatus;
import com.example.bookMyShow.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    private final Random random = new Random();


    @Transactional
    public Payment processPayment(User user, Ticket ticket, PaymentMethod paymentMethod, double amount) throws PaymentProcessingException {
        if(amount <= 0){
            throw  new PaymentProcessingException("Payment amount must be greater than 0");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setTicket(ticket);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setTransactionId(generateTransactionId());

        payment = paymentRepository.save(payment);

        /*
        Why that try … catch block exists
        External‐gateway risk
        processWithPaymentGateway() talks to a bank/PSP, so it can:

        return false (declined)

        throw an exception (timeout, 5xx, network error)

        Update domain state
        Whatever happens must be reflected in the Payment entity:

        SUCCESS → set completedAt, success message

        Any failure → mark FAILED, capture gateway response

        Persist the outcome
        The method writes the status back to the database (paymentRepository.save(payment)) so the rest of the system sees the final state.

        Propagate a clean error
        Instead of leaking low‑level IO/JSON exceptions up the stack, it wraps everything in a business‑level PaymentProcessingException.
         */

        try{
            //Simulate payment processing with external payment gateway
            boolean paymentSuccess = processWithPaymentGateway(payment);

            if(paymentSuccess){
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setGatewayResponse("Payment processed Successfully");
            }
            else{
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setGatewayResponse("Payment declined by bank");
            }

        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse("Payment Processing error: " + e.getMessage());
            throw new PaymentProcessingException("Payment failed: " + payment.getGatewayResponse());
        }
        return paymentRepository.save(payment);
    }
    /**
     * Process refund for a cancelled ticket
     * @param originalPayment The original payment to refund
     * @param refundAmount The amount to refund
     * @return Payment object representing the refund
     */

    @Transactional
    public Payment processRefund(Payment originalPayment, double refundAmount) throws PaymentProcessingException {
        if(refundAmount <= 0 || refundAmount > originalPayment.getAmount()){
            throw new PaymentProcessingException("Invalid refund amount");
        }

        // check if original payment was successful
        if(!originalPayment.getPaymentStatus().equals(PaymentStatus.SUCCESS)){
            throw new PaymentProcessingException("Cannot refund unsuccessful payment");
        }

        // create refund record
        Payment refund = new Payment();
        refund.setUser(originalPayment.getUser());
        refund.setTicket(originalPayment.getTicket());
        refund.setAmount(-refundAmount);
        refund.setPaymentMethod(originalPayment.getPaymentMethod());
        refund.setPaymentStatus(PaymentStatus.PENDING);
        refund.setCreatedAt(LocalDateTime.now());
        refund.setTransactionId(generateTransactionId());
        refund.setOriginalPayment(originalPayment);

        refund = paymentRepository.save(refund);

        try {
            // Simulate refund processing with external payment gateway
            boolean refundSuccess = processRefundWithPaymentGateway(refund);

            if (refundSuccess) {
                refund.setPaymentStatus(PaymentStatus.SUCCESS);
                refund.setCompletedAt(LocalDateTime.now());
                refund.setGatewayResponse("Refund processed successfully");
            } else {
                refund.setPaymentStatus(PaymentStatus.FAILED);
                refund.setGatewayResponse("Refund failed");
                throw new PaymentProcessingException("Refund failed: " + refund.getGatewayResponse());
            }

        } catch (Exception e) {
            refund.setPaymentStatus(PaymentStatus.FAILED);
            refund.setGatewayResponse("Refund processing error: " + e.getMessage());
            paymentRepository.save(refund);
            throw new PaymentProcessingException("Refund processing failed: " + e.getMessage());
        }

        return paymentRepository.save(refund);
    }
    /**
     * Get payment by ID
     * @param id Payment ID
     * @return Payment object
     */

    public Payment getPaymentById(int id){
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException("Payment with id " + id + " not found")
        );
    }

    /**
     * Get all payments for a user
     * @param userId User ID
     * @return List of payments
     */

    public List<Payment> getPaymentsByUserId(int userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Get all payments for a ticket
     * @param ticketId Ticket ID
     * @return List of payments
     */
    public List<Payment> getPaymentsByTicketId(int ticketId) {
        return paymentRepository.findByTicketId(ticketId);
    }
    /**
     * Get all payments
     * @return List of all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payments by status
     * @param status Payment status
     * @return List of payments with the specified status
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }


    /**
     * Cancel a pending payment
     * @param paymentId Payment ID
     * @return Updated payment object
     */
    @Transactional
    public Payment cancelPayment(int paymentId) throws PaymentProcessingException {
        Payment payment = getPaymentById(paymentId);

        if (!payment.getPaymentStatus().equals(PaymentStatus.PENDING)) {
            throw new PaymentProcessingException("Can only cancel pending payments");
        }

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setGatewayResponse("Payment cancelled by user");
        payment.setCompletedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Verify payment status with payment gateway
     * @param paymentId Payment ID
     * @return Updated payment object
     */
    @Transactional
    public Payment verifyPaymentStatus(int paymentId) throws PaymentProcessingException {
        Payment payment = getPaymentById(paymentId);

        if (payment.getPaymentStatus().equals(PaymentStatus.SUCCESS) ||
                payment.getPaymentStatus().equals(PaymentStatus.FAILED)) {
            return payment; // Already processed
        }

        try {
            // Simulate verification with payment gateway
            PaymentStatus verifiedStatus = verifyWithPaymentGateway(payment.getTransactionId());

            if (!payment.getPaymentStatus().equals(verifiedStatus)) {
                payment.setPaymentStatus(verifiedStatus);
                if (verifiedStatus.equals(PaymentStatus.SUCCESS) ||
                        verifiedStatus.equals(PaymentStatus.FAILED)) {
                    payment.setCompletedAt(LocalDateTime.now());
                }
                payment = paymentRepository.save(payment);
            }

        } catch (Exception e) {
            throw new PaymentProcessingException("Payment verification failed: " + e.getMessage());
        }

        return payment;
    }

    /**
     * Calculate total amount for a ticket including taxes and fees
     * @param baseAmount Base ticket amount
     * @return Total amount including taxes and fees
     */
    public double calculateTotalAmount(double baseAmount) {
        double serviceCharge = baseAmount * 0.05; // 5% service charge
        double tax = baseAmount * 0.18; // 18% GST
        double processingFee = 10.0; // Fixed processing fee

        return baseAmount + serviceCharge + tax + processingFee;
    }

    // Private helper methods

    /**
     * Generate unique transaction ID
     * @return Transaction ID
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + random.nextInt(1000);
    }

    /**
     * Simulate payment processing with external payment gateway
     * @param payment Payment object
     * @return Success status
     */
    private boolean processWithPaymentGateway(Payment payment) {
        // Simulate payment gateway processing
        try {
            Thread.sleep(1000); // Simulate network delay

            // Simulate success/failure (90% success rate)
            return random.nextInt(10) < 9;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Simulate refund processing with external payment gateway
     * @param refund Refund payment object
     * @return Success status
     */
    private boolean processRefundWithPaymentGateway(Payment refund) {
        // Simulate refund gateway processing
        try {
            Thread.sleep(1000); // Simulate network delay

            // Simulate success/failure (95% success rate for refunds)
            return random.nextInt(10) < 9.5;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Simulate payment status verification with external payment gateway
     * @param transactionId Transaction ID
     * @return Payment status
     */
    private PaymentStatus verifyWithPaymentGateway(String transactionId) {
        // Simulate gateway verification
        try {
            Thread.sleep(500); // Simulate network delay

            // Simulate different statuses
            int statusCode = random.nextInt(10);
            if (statusCode < 7) {
                return PaymentStatus.SUCCESS;
            } else if (statusCode < 9) {
                return PaymentStatus.FAILED;
            } else {
                return PaymentStatus.PENDING;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentStatus.FAILED;
        }
    }
}



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
