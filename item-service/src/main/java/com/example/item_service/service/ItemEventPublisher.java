package com.example.item_service.service;

import com.example.item_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemEventPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendItemCreatedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_EXCHANGE, RabbitMQConfig.ITEM_CREATED_ROUTING_KEY, userId);
    }

    public void sendItemUpdatedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_EXCHANGE, RabbitMQConfig.ITEM_UPDATED_ROUTING_KEY, userId);
    }

    public void sendItemDeletedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_EXCHANGE, RabbitMQConfig.ITEM_DELETED_ROUTING_KEY, userId);
    }
    public void sendItemQueriedEvent(String userId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_EXCHANGE, RabbitMQConfig.ITEM_QUERIED_ROUTING_KEY, userId);
    }
}
