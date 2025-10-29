package com.gal.usc.roomify.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "¡Hola! Tu aplicación Spring Boot está corriendo correctamente 🎉";
    }
}