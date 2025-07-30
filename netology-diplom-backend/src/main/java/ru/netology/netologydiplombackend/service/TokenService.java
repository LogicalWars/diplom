package ru.netology.netologydiplombackend.service;

public interface TokenService {
    void save(String token, String username);

    String getLoginByToken(String token);

    void delete(String token);

    boolean exists(String token);
}
