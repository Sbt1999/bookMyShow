package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.ShowSeat;
import com.example.bookMyShow.service.ShowSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShowSeatController {
    @Autowired
    private ShowSeatService showSeatService;

    @PostMapping("/showSeat")
    public ResponseEntity<ShowSeat> createShowSeat(@RequestBody ShowSeat showSeat) {
        return ResponseEntity.ok(showSeatService.createShowSeat(showSeat));
    }

    @GetMapping("/showSeat/{id}")
    public ResponseEntity<ShowSeat> getShowSeat(@PathVariable("id") int id) {
        return ResponseEntity.ok(showSeatService.getShowSeatById(id));
    }

    @GetMapping("/showSeats")
    public ResponseEntity<List<ShowSeat>> getAllShowSeats() {
        return ResponseEntity.ok(showSeatService.getAllShowSeats());
    }

    @DeleteMapping("/showSeat/{id}")
    public ResponseEntity<Boolean> removeShowSeat(@PathVariable("id") int id) {
        showSeatService.deleteShowSeatById(id);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/showSeat")
    public ResponseEntity<ShowSeat> updateShowSeat(@RequestBody ShowSeat showSeat) {
        return ResponseEntity.ok(showSeatService.updateShowSeat(showSeat));
    }
}
