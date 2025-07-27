package ru.netology.netologydiplombackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.netologydiplombackend.dto.login.LoginRequest;
import ru.netology.netologydiplombackend.dto.login.LoginResponse;
import ru.netology.netologydiplombackend.exception.InvalidCredentialsException;
import ru.netology.netologydiplombackend.service.AuthService;
import ru.netology.netologydiplombackend.service.TokenService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        if (authService.authenticate(request.getLogin(), request.getPassword())) {
            String token = authService.generateToken();
            tokenService.save(token, request.getLogin());
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            return ResponseEntity.ok(loginResponse);
        } else {
            throw new InvalidCredentialsException("Invalid credentials", 40001);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
            tokenService.delete(token);
            return ResponseEntity.ok("Logged out");
    }
}

