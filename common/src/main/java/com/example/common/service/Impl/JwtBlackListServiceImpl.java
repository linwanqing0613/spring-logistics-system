package com.example.common.service.Impl;

import com.example.common.service.JwtBlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlackListServiceImpl implements JwtBlackListService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    public void addToBlackList(String jti, long expirationSeconds) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "true",
                expirationSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean isTokenBlackList(String jti) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
    }
}
