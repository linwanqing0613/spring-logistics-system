package com.example.userservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_REGISTERED_QUEUE = "user.registered.queue";
    public static final String USER_UPDATED_QUEUE = "user.updated.queue";
    public static final String USER_DELETED_QUEUE = "user.deleted.queue";
    public static final String USER_QUERIED_QUEUE = "user.queried.queue";

    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_DELETED_ROUTING_KEY = "user.deleted";
    public static final String USER_QUERIED_ROUTING_KEY = "user.queried";
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Queue userUpdatedQueue() {
        return new Queue(USER_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue userDeletedQueue() {
        return new Queue(USER_DELETED_QUEUE, true);
    }
    @Bean
    public Queue userQueriedQueue() {
        return new Queue(USER_QUERIED_QUEUE, true);
    }

    @Bean
    public Binding bindingRegistered(Queue userRegisteredQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with(USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingUpdated(Queue userUpdatedQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userUpdatedQueue).to(userExchange).with(USER_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDeleted(Queue userDeletedQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userDeletedQueue).to(userExchange).with(USER_DELETED_ROUTING_KEY);
    }
    @Bean
    public Binding bindingQueried(Queue userQueriedQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userQueriedQueue).to(userExchange).with(USER_QUERIED_ROUTING_KEY);
    }
}
