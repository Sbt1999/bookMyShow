package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.CityNotFoundException;
import com.example.bookMyShow.model.City;
import com.example.bookMyShow.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {
    @Autowired
    private CityRepository cityRepository;

    public City createCity(City city) {
        return cityRepository.save(city);
    }

    public City getCityById(long id) {
        return cityRepository.findById(id).orElseThrow(
                () -> new CityNotFoundException("City with id " + id + " not found")
        );
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public void deleteCityById(long id) {
        cityRepository.deleteById(id);
    }
}
