package ru.netology.netologydiplombackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.netology.netologydiplombackend.service.AuthService;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final AuthService authService;

    @GetMapping("/secret")
    public String secret() {
        return "This is protected";
    }
}
