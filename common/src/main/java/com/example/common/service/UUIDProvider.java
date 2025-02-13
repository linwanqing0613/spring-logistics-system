package com.example.common.service;

import com.example.common.dto.ModelName;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDProvider {
    public String generateUUID(ModelName modelName) {
        return modelName.getPrefix() + UUID.randomUUID().toString();
    }
}
