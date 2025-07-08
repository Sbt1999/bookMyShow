package com.example.bookMyShow.model;

import com.example.bookMyShow.model.constant.ShowStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Showw extends BaseModel{
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String language;
    @ManyToOne
    private Auditorium auditorium;
    @ManyToOne
    private Movie movie;
    @OneToMany
    private List<ShowSeat> showSeats;
    @Enumerated(EnumType.STRING)
    private ShowStatus showStatus;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public void setAuditorium(Auditorium auditorium) {
        this.auditorium = auditorium;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public List<ShowSeat> getShowSeats() {
        return showSeats;
    }

    public void setShowSeats(List<ShowSeat> showSeats) {
        this.showSeats = showSeats;
    }

    public ShowStatus getShowStatus() {
        return showStatus;
    }

    public void setShowStatus(ShowStatus showStatus) {
        this.showStatus = showStatus;
    }
}
