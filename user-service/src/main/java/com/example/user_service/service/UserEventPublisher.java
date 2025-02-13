package com.example.userservice.service;

import com.example.userservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendUserRegisteredEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_REGISTERED_ROUTING_KEY, userId);
    }

    public void sendUserUpdatedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_UPDATED_ROUTING_KEY, userId);
    }

    public void sendUserDeletedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_DELETED_ROUTING_KEY, userId);
    }
    public void sendUserQueriedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_QUERIED_ROUTING_KEY, userId);
    }
}
