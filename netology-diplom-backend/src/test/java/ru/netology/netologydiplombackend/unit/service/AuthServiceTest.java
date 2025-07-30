package ru.netology.netologydiplombackend.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.UserRepository;
import ru.netology.netologydiplombackend.service.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void testAuthenticateSuccess() {
        User user = new User();
        user.setLogin("admin");
        user.setPassword("encodedPassword");

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin", "encodedPassword")).thenReturn(true);

        boolean result = authService.authenticate("admin", "admin");

        assertTrue(result);
    }

    @Test
    void testAuthenticateFailure() {
        when(userRepository.findByLogin("admin")).thenReturn(Optional.empty());

        boolean result = authService.authenticate("admin", "admin");

        assertFalse(result);
    }

    @Test
    void testGenerateToken() {
        String token = authService.generateToken();
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}

