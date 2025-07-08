package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.Auditorium;
import com.example.bookMyShow.service.AuditoriumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuditoriumController {
    @Autowired
    private AuditoriumService auditoriumService;

    @GetMapping("/auditorium/{id}")
    public ResponseEntity<Auditorium> getAuditorium(@PathVariable("id") int id) {
        return ResponseEntity.ok(auditoriumService.getAuditoriumById(id));
    }

    @PostMapping("/auditorium")
    public ResponseEntity<Auditorium> createAuditorium(@RequestBody Auditorium auditorium) {
        return ResponseEntity.ok(auditoriumService.createAuditorium(auditorium));
    }

    @GetMapping("/auditoriums")
    public ResponseEntity<List<Auditorium>> getAllAuditoriums() {
        return ResponseEntity.ok(auditoriumService.getAllAuditoriums());
    }

    @DeleteMapping("/auditorium/{id}")
    public ResponseEntity<Boolean> removeAuditorium(@PathVariable("id") int id) {
        auditoriumService.deleteAuditoriumById(id);
        return ResponseEntity.ok(true);
    }
}
