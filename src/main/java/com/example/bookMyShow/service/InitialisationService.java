package com.example.bookMyShow.service;

import com.example.bookMyShow.model.*;
import com.example.bookMyShow.model.constant.SeatStatus;
import com.example.bookMyShow.model.constant.SeatType;
import com.example.bookMyShow.model.constant.ShowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
This service only use to hard code the data --> in start time when  u r building the app..
 */
@Service
public class InitialisationService {


    @Autowired
    private CityService cityService;
    @Autowired
    private TheatreService theatreService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private AuditoriumService auditoriumService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private ShowService showService;

    public void initialise(){
        //create seats
        List<Seat> seats = new ArrayList<>();
        for(int i=1;i<=100;i++){
            Seat seat = new Seat();
            seat.setSeatNumber("Seat - " + i);
            seat.setRow(i); // just an assumption to create data
            seat.setCol(i); // just an assumption to create data
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seat.setSeatType(SeatType.GOLD);
            seat = seatService.createSeat(seat);
            seats.add(seat);
        }

        //create auditorium
        Auditorium auditorium = new Auditorium();
        auditorium.setName("AUDI 01");
        auditorium.setCapacity(100);
        auditorium.setSeats(seats);
        auditorium = auditoriumService.createAuditorium(auditorium);

        //create theatre
        Theatre theatre = new Theatre();
        theatre.setName("PVR INOX CINEPOLIS");
        theatre.setAddress("Road 1, City 2, Bangalore - 1234456");
        theatre.setAuditoriums(List.of(auditorium));
        theatre = theatreService.createTheatre(theatre);

        //create city
        City city = new City();
        city.setName("Bangalore");
        city.setTheatres(List.of(theatre));
        city = cityService.createCity(city);

        //create Movie
        Movie movie = new Movie();
        movie.setName("Spiderman chala bihar");
        movie.setLanguage("Bhojpuri");
        movie = movieService.addMovie(movie);

        //create show
        Showw show = new Showw();
        show.setMovie(movie);
        show.setAuditorium(auditorium);
        show.setLanguage("Bhojpuri");

        show.setShowStatus(ShowStatus.YET_TO_START);
        show.setStartTime(LocalDateTime.now()); // just an assumption to create data
        show.setEndTime(LocalDateTime.now());  // just an assumption to create data
        show = showService.createShow(show);
    }
}