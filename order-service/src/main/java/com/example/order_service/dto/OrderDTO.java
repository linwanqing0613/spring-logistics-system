package com.example.order_service.dto;

import jakarta.persistence.Id;

import java.time.LocalDateTime;

public class OrderDTO {
    @Id
    private String id;
    private String userId;
    private String status;

}
