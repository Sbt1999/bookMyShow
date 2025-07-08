package com.example.bookMyShow.controller;

import com.example.bookMyShow.model.User;
import com.example.bookMyShow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/user/{id}")
    public ResponseEntity findUserById(@PathVariable("id") int id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/user")
    public ResponseEntity createUser(@RequestBody User user){
        return ResponseEntity.ok(userService.createUser(user));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity deleteUser(@PathVariable("id") int id){
        userService.deleteUserById(id);
        return ResponseEntity.ok(true);
    }
}
