package com.example.common.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDProvider {
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
