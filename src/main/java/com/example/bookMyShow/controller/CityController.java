package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.City;
import com.example.bookMyShow.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping("/city/{id}")
    public ResponseEntity getCity(@PathVariable("id") Long id){
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @PostMapping("/city")
    public ResponseEntity createCity(@RequestBody City city){
        return ResponseEntity.ok(cityService.createCity(city));
    }

    @DeleteMapping("/city/{id}")
    public ResponseEntity removeCity(@PathVariable("id") Long id){
        cityService.deleteCityById(id);
        return ResponseEntity.ok(true);
    }
}
