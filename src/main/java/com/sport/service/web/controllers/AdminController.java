package com.sport.service.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/admin")
    public String test() {
        return "Test, look here!";
    }
}