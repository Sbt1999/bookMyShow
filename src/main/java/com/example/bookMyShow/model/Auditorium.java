package com.example.bookMyShow.model;

import com.example.bookMyShow.model.constant.AuditoriumFeatures;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Auditorium extends BaseModel{
    private String name;
    private int capacity;
    @OneToMany
    private List<Seat> seats;
    @OneToMany
    private List<Showw> shows;
    @ElementCollection // creates a relationship btw auditorium and enum table
    @Enumerated(EnumType.STRING) // creates a table for values present and enum
    private List<AuditoriumFeatures> auditoriumFeatures;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public List<Showw> getShows() {
        return shows;
    }

    public void setShows(List<Showw> shows) {
        this.shows = shows;
    }

    public List<AuditoriumFeatures> getAuditoriumFeatures() {
        return auditoriumFeatures;
    }

    public void setAuditoriumFeatures(List<AuditoriumFeatures> auditoriumFeatures) {
        this.auditoriumFeatures = auditoriumFeatures;
    }
}
