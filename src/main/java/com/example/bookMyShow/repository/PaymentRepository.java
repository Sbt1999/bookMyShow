package com.example.bookMyShow.repository;

import com.example.bookMyShow.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // How JPA MAP Entity to DB tables?
}
