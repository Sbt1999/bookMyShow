package com.example.bookMyShow.controller;

import com.example.bookMyShow.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @GetMapping("/hello")
    public String hello(){
        return "HELLO! My baby";
    }
}
