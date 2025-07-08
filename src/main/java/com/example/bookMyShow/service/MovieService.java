package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.MovieNotFoundException;
import com.example.bookMyShow.model.Movie;
import com.example.bookMyShow.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie getMovieById(int id) {
        return movieRepository.findById(id).orElseThrow(
                () -> new MovieNotFoundException("Movie not found with id: " + id)
        );
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public void deleteMovieById(int id) {
        movieRepository.deleteById(id);
    }
}
