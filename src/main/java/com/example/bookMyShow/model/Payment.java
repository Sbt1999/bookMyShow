package com.example.bookMyShow.model;

import com.example.bookMyShow.model.constant.PaymentMethod;
import com.example.bookMyShow.model.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Payment extends BaseModel{

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column
    private String gatewayResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    // For refund payments, reference to original payment
    @ManyToOne
    @JoinColumn(name = "original_payment_id")
    private Payment originalPayment;

    @Column
    private String gatewayTransactionId; // ID from payment gateway

    @Column
    private String failureReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Payment getOriginalPayment() {
        return originalPayment;
    }

    public void setOriginalPayment(Payment originalPayment) {
        this.originalPayment = originalPayment;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}























//
//// Payment.java
//package com.example.bookMyShow.model;
//
//import com.example.bookMyShow.model.constant.PaymentMethod;
//import com.example.bookMyShow.model.constant.PaymentStatus;
//import jakarta.persistence.*;
//        import lombok.Getter;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "payments")
//public class Payment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private int id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "ticket_id", nullable = false)
//    private Ticket ticket;
//
//    @Column(nullable = false)
//    private double amount;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private PaymentMethod paymentMethod;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private PaymentStatus paymentStatus;
//
//    @Column(nullable = false, unique = true)
//    private String transactionId;
//
//    @Column
//    private String gatewayResponse;
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column
//    private LocalDateTime completedAt;
//
//    // For refund payments, reference to original payment
//    @ManyToOne
//    @JoinColumn(name = "original_payment_id")
//    private Payment originalPayment;
//
//    @Column
//    private String gatewayTransactionId; // ID from payment gateway
//
//    @Column
//    private String failureReason;
//
//    @PrePersist
//    protected void onCreate() {
//        if (createdAt == null) {
//            createdAt = LocalDateTime.now();
//        }
//    }
//}
//
//// PaymentStatus.java
//package com.example.bookMyShow.model.constant;
//
//public enum PaymentStatus {
//    PENDING,
//    SUCCESS,
//    FAILED,
//    CANCELLED,
//    REFUNDED
//}
//
//// PaymentMethod.java
//package com.example.bookMyShow.model.constant;
//
//public enum PaymentMethod {
//    CREDIT_CARD,
//    DEBIT_CARD,
//    UPI,
//    NET_BANKING,
//    WALLET,
//    CASH_ON_DELIVERY
//}
//
//// PaymentProcessingException.java
//package com.example.bookMyShow.exception;
//
//public class PaymentProcessingException extends Exception {
//    public PaymentProcessingException(String message) {
//        super(message);
//    }
//
//    public PaymentProcessingException(String message, Throwable cause) {
//        super(message, cause);
//    }
//}
//
//// PaymentNotFoundException.java
//package com.example.bookMyShow.exception;
//
//public class PaymentNotFoundException extends RuntimeException {
//    public PaymentNotFoundException(String message) {
//        super(message);
//    }
//
//    public PaymentNotFoundException(String message, Throwable cause) {
//        super(message, cause);
//    }
//}
//
//// PaymentRepository.java
//package com.example.bookMyShow.repository;
//
//import com.example.bookMyShow.model.Payment;
//import com.example.bookMyShow.model.constant.PaymentStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface PaymentRepository extends JpaRepository<Payment, Integer> {
//
//    List<Payment> findByUserId(int userId);
//
//    List<Payment> findByTicketId(int ticketId);
//
//    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
//
//    Optional<Payment> findByTransactionId(String transactionId);
//
//    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus = :status")
//    List<Payment> findByUserIdAndPaymentStatus(@Param("userId") int userId,
//                                               @Param("status") PaymentStatus status);
//
//    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
//    List<Payment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
//                                         @Param("endDate") LocalDateTime endDate);
//
//    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
//    Double getTotalSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate,
//                                             @Param("endDate") LocalDateTime endDate);
//}
