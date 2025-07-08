package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.Theatre;
import com.example.bookMyShow.service.TheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TheaterController {

    @Autowired
    private TheatreService theatreService;

    @GetMapping("/theater/{id}")
    public ResponseEntity<Theatre> getTheater(@PathVariable("id") int id) {
        return ResponseEntity.ok(theatreService.getTheatreById(id));
    }

    @PostMapping("/theater")
    public ResponseEntity<Theatre> createTheater(@RequestBody Theatre theatre) {
        return ResponseEntity.ok(theatreService.createTheatre(theatre));
    }

    @GetMapping("/theaters")
    public ResponseEntity<List<Theatre>> getAllTheaters() {
        return ResponseEntity.ok(theatreService.getAllTheatres());
    }

    @DeleteMapping("/theater/{id}")
    public ResponseEntity<Boolean> removeTheater(@PathVariable("id") int id) {
        theatreService.deleteTheatreById(id);
        return ResponseEntity.ok(true);
    }
}
