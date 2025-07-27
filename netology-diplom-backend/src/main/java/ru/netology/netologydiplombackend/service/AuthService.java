package ru.netology.netologydiplombackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.netology.netologydiplombackend.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean authenticate(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .isPresent();
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
