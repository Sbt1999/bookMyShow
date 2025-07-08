package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.Movie;
import com.example.bookMyShow.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MovieController {
    @Autowired
    private MovieService movieService;
    @GetMapping("/movie/{id}")
    // ID is a variable so here I will put that's why we put it in bracket
    //Below Id is comming from ID path so I will add @PathVariable
    public ResponseEntity findMovieById(@PathVariable("id") int id){
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    // for creating the movie
    @PostMapping("/movie")
    public ResponseEntity createMovie(@RequestBody Movie movie){
        return ResponseEntity.ok(movieService.addMovie(movie));
    }
    @DeleteMapping("/movie/{id}")
    public ResponseEntity deleteMovie(@PathVariable("id") int id){
        movieService.deleteMovieById(id);
        return ResponseEntity.ok(true);
    }
}
