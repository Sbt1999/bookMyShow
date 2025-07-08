package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.SeatNotFoundException;
import com.example.bookMyShow.model.Seat;
import com.example.bookMyShow.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    @Autowired
    private SeatRepository seatRepository;

    public Seat createSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    public Seat getSeatById(int id) {
        return seatRepository.findById(id).orElseThrow(
                () -> new SeatNotFoundException("Seat with id " + id + " not found")
        );
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public void deleteSeatById(int id) {
        seatRepository.deleteById(id);
    }
}
