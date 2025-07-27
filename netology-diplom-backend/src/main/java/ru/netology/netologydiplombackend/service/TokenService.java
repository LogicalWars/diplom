package ru.netology.netologydiplombackend.service;

public interface TokenService {
    void save(String token, String username);

    String getUsernameByToken(String token);

    void delete(String token);

    boolean exists(String token);
}
