package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.ShowNotFoundException;
import com.example.bookMyShow.model.Seat;
import com.example.bookMyShow.model.ShowSeat;
import com.example.bookMyShow.model.Showw;
import com.example.bookMyShow.model.constant.ShowSeatStatus;
import com.example.bookMyShow.repository.ShowRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private ShowSeatService showSeatService;

    public Showw createShow(Showw show) {
        //create the showSeats for the show
        List<ShowSeat> showSeats = new ArrayList<>();
        show = showRepository.save(show);

        List<Seat> seats = show.getAuditorium().getSeats();
        for (Seat seat : seats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setSeat(seat);
            showSeat.setPrice(100);
            showSeat.setShow(show);
            showSeat.setShowSeatStatus(ShowSeatStatus.AVAILABLE);
            showSeat = showSeatService.createShowSeat(showSeat);
            showSeats.add(showSeat);
        }
        show.setShowSeats(showSeats);
        return showRepository.save(show);
    }

    public Showw getShowById(int id) {
        return showRepository.findById(id).orElseThrow(
                () -> new ShowNotFoundException("Show with id " + id + " not found")
        );
    }

    public List<Showw> getAllShows() {
        return showRepository.findAll();
    }

    public void deleteShowById(int id) {
        showRepository.deleteById(id);
    }
}
