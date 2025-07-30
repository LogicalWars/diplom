package ru.netology.netologydiplombackend.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.netologydiplombackend.dto.login.LoginRequest;
import ru.netology.netologydiplombackend.dto.login.LoginResponse;
import ru.netology.netologydiplombackend.integration.TestcontainersConfiguration;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String LOGIN = "testLogin";
    private static final String PASSWORD = "testPassword";
    private static final String URL = "/login";

    private final User user = new User();

    @BeforeEach
    void setupUser() {
        user.setLogin(LOGIN);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        if (userRepository.findByLogin(LOGIN).isEmpty()) {
            userRepository.save(user);
        }
    }

    @Test
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(LOGIN);
        loginRequest.setPassword(PASSWORD);

        ResponseEntity<LoginResponse> response = restTemplate
                .postForEntity(URL, loginRequest, LoginResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testLoginFailure() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(LOGIN);
        loginRequest.setPassword("wrong");

        ResponseEntity<LoginResponse> response = restTemplate
                .postForEntity(URL, loginRequest, LoginResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

