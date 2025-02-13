package com.example.item_service.event;

import com.example.item_service.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ItemEventListener {

    private static final Logger log = LoggerFactory.getLogger(ItemEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.ITEM_CREATED_QUEUE)
    public void handleItemCreatedListenerEvent(String userId){
        log.info("RabbitMQ item.Created.queue: {}", userId);
    }
    @RabbitListener(queues = RabbitMQConfig.ITEM_UPDATED_QUEUE)
    public void handleItemUpdatedEvent(String userId){
        log.info("RabbitMQ item.updated.queue: {}", userId);
    }
    @RabbitListener(queues = RabbitMQConfig.ITEM_DELETED_QUEUE)
    public void handleItemDeletedEvent(String userId){
        log.info("RabbitMQ item.deleted.queue: {}", userId);
    }
    @RabbitListener(queues = RabbitMQConfig.ITEM_QUERIED_QUEUE)
    public void handleItemQueriedEvent(String userId){
        log.info("RabbitMQ item.queried.queue: {}", userId);
    }
}
