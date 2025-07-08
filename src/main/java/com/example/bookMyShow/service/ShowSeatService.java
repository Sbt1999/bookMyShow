package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.ShowSeatNotFoundException;
import com.example.bookMyShow.model.ShowSeat;
import com.example.bookMyShow.repository.ShowSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowSeatService {

    @Autowired
    private ShowSeatRepository showSeatRepository;


    public ShowSeat createShowSeat(ShowSeat showSeat) {
        return showSeatRepository.save(showSeat);
    }

    public ShowSeat getShowSeatById(int id) {
        return showSeatRepository.findById(id).orElseThrow(
                () -> new ShowSeatNotFoundException("ShowSeat with id " + id + " not found")
        );
    }

    public List<ShowSeat> getAllShowSeats() {
        return showSeatRepository.findAll();
    }

    public void deleteShowSeatById(int id) {
        showSeatRepository.deleteById(id);
    }

    public ShowSeat updateShowSeat(ShowSeat showSeat) {
        return showSeatRepository.save(showSeat);
    }
}
