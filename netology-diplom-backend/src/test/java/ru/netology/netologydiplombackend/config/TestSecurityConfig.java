package ru.netology.netologydiplombackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.netology.netologydiplombackend.service.TokenService;

@TestConfiguration
public class TestSecurityConfig {

    public static final String LOGIN = "testLogin";

    @Bean
    @Primary
    public TokenService testTokenService() {
        return new TokenService() {
            @Override
            public boolean exists(String token) {
                return true;
            }

            @Override
            public void save(String token, String username) {}

            @Override
            public String getLoginByToken(String token) {
                return LOGIN;
            }

            @Override
            public void delete(String token) {}
        };
    }

}
