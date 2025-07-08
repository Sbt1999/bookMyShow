package com.example.bookMyShow.repository;

import com.example.bookMyShow.model.Showw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Showw, Integer> {
}
