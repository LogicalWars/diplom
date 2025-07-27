package ru.netology.netologydiplombackend.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    public boolean authenticate(String login, String password) {
        return "admin".equals(login) && "admin".equals(password);
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
