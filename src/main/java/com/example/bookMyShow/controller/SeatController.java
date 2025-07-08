package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.Seat;
import com.example.bookMyShow.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SeatController {
    @Autowired
    private SeatService seatService;

    @GetMapping("/seat/{id}")
    public ResponseEntity<Seat> getSeat(@PathVariable("id") int id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @PostMapping("/seat")
    public ResponseEntity<Seat> createSeat(@RequestBody Seat seat) {
        return ResponseEntity.ok(seatService.createSeat(seat));
    }

    @GetMapping("/seats")
    public ResponseEntity<List<Seat>> getAllSeats() {
        return ResponseEntity.ok(seatService.getAllSeats());
    }

    @DeleteMapping("/seat/{id}")
    public ResponseEntity<Boolean> removeSeat(@PathVariable("id") int id) {
        seatService.deleteSeatById(id);
        return ResponseEntity.ok(true);
    }
}
