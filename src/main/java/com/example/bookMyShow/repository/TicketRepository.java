package com.example.bookMyShow.repository;

import com.example.bookMyShow.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/*
Whenever @Repository is used springBoot says
I need to create an object of that and keep it available whereEver U inject I will use it.
 */
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
