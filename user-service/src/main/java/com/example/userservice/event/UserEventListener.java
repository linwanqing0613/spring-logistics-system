package com.example.userservice.event;

import com.example.userservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void handleUserRegisterListenerEvent(String userId){
        log.info("RabbitMQ registered.queue: {}", userId);
    }
    @RabbitListener(queues = RabbitMQConfig.USER_UPDATED_QUEUE)
    public void handleUserUpdatedEvent(String userId){
        log.info("RabbitMQ updated.queue: {}", userId);
    }
    @RabbitListener(queues = RabbitMQConfig.USER_DELETED_QUEUE)
    public void handleUserDeletedEvent(String userId){
        log.info("RabbitMQ deleted.queue: {}", userId);
    }
    @RabbitListener(queues = RabbitMQConfig.USER_QUERIED_QUEUE)
    public void handleUserQueriedEvent(String userId){
        log.info("RabbitMQ queried.queue: {}", userId);
    }
}
