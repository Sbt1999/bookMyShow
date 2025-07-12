package com.example.bookMyShow.repository;

import com.example.bookMyShow.model.Payment;
import com.example.bookMyShow.model.constant.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // How JPA MAP Entity to DB tables?
    List<Payment> findByUserId(int userId);

    List<Payment> findByTicketId(int ticketId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus = :status")
    List<Payment> findByUserIdAndPaymentStatus(@Param("userId") int userId,
                                               @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
