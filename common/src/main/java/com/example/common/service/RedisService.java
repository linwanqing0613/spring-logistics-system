package com.example.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface RedisService {
    public void save(String key, Object value);
    public <T> T get(String key, Class<T> tClass);
    public void delete(String key);
    public void update(String key, Object value);
    public boolean acquireLock(String lockKey, String requestId, long expireTime);
    public boolean releaseLock(String lockKey, String requestId);
}
