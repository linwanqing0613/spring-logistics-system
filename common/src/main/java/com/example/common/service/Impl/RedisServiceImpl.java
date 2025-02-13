package com.example.common.service.Impl;

import com.example.common.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private static final long TIMEOUT = 36000L;
    public void save(String key, Object object){
        try {
            String value = objectMapper.writeValueAsString(object);
            redisTemplate.opsForValue().set(key, value, TIMEOUT, TimeUnit.SECONDS);
        }catch (JsonProcessingException e){
            throw new RuntimeException("Failed to serialize Item", e);
        }
    }
    public <T> T get(String key, Class<T> Class){
        try {
            String jsonString = redisTemplate.opsForValue().get(key).toString();
            if(jsonString != null){
                return objectMapper.readValue(jsonString, Class);
            }
            return null;
        } catch (JsonProcessingException e){
            throw new RuntimeException("Failed to deserialize item", e);
        }
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
    public void update(String key, Object object) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            save(key, object);
        } else {
            throw new RuntimeException("Key not found in Redis: " + key);
        }
    }
    public boolean acquireLock(String lockKey, String requestId, long expireTime) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public boolean releaseLock(String lockKey, String requestId) {
        Object value = redisTemplate.opsForValue().get(lockKey);
        if (requestId.equals(value)) {
            redisTemplate.delete(lockKey);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}