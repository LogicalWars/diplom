package ru.netology.netologydiplombackend.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        String login = "admin";
        String password = "admin";

        if (userRepository.findByLogin(login).isEmpty()) {
            User admin = new User();
            admin.setLogin(login);
            admin.setPassword(passwordEncoder.encode(password));
            userRepository.save(admin);

            System.out.println("Admin user created");
        } else {
            System.out.println("Admin user already exists");
        }
    }
}
