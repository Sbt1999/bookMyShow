package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.AuditoriumNotFoundException;
import com.example.bookMyShow.model.Auditorium;
import com.example.bookMyShow.repository.AuditoriumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class AuditoriumService {
    @Autowired
    private AuditoriumRepository auditoriumRepository;

    public Auditorium createAuditorium(Auditorium auditorium) {
        return auditoriumRepository.save(auditorium);
    }

    public Auditorium getAuditoriumById(int id) {
        return auditoriumRepository.findById(id).orElseThrow(
                () -> new AuditoriumNotFoundException("Auditorium with id " + id + " not found")
        );
    }

    public List<Auditorium> getAllAuditoriums() {
        return auditoriumRepository.findAll();
    }

    public void deleteAuditoriumById(int id) {
        auditoriumRepository.deleteById(id);
    }
}
