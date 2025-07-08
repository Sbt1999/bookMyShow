package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.Showw;
import com.example.bookMyShow.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShowController {
    @Autowired
    private ShowService showService;

    @PostMapping("/show")
    public ResponseEntity<Showw> createShow(@RequestBody Showw show) {
        return ResponseEntity.ok(showService.createShow(show));
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<Showw> getShow(@PathVariable("id") int id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/shows")
    public ResponseEntity<List<Showw>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @DeleteMapping("/show/{id}")
    public ResponseEntity<Boolean> removeShow(@PathVariable("id") int id) {
        showService.deleteShowById(id);
        return ResponseEntity.ok(true);
    }

}
