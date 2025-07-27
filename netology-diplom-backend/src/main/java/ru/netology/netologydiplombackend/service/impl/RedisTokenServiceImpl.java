package ru.netology.netologydiplombackend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.netology.netologydiplombackend.service.TokenService;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenServiceImpl implements TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.token.time-to-live:3600}")
    private int ttl;

    public RedisTokenServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String token, String username) {
        redisTemplate.opsForValue().set(token, username, ttl, TimeUnit.SECONDS);
    }

    @Override
    public String getUsernameByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    @Override
    public void delete(String token) {
        redisTemplate.delete(token);
    }

    @Override
    public boolean exists(String token) {
        return redisTemplate.hasKey(token);
    }
}
