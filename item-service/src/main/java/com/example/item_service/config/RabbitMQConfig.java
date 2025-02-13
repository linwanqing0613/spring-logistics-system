package com.example.item_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String ITEM_EXCHANGE = "item.exchange";
    public static final String ITEM_CREATED_QUEUE = "item.created.queue";
    public static final String ITEM_UPDATED_QUEUE = "item.updated.queue";
    public static final String ITEM_DELETED_QUEUE = "item.deleted.queue";
    public static final String ITEM_QUERIED_QUEUE = "item.queried.queue";

    public static final String ITEM_CREATED_ROUTING_KEY = "item.created";
    public static final String ITEM_UPDATED_ROUTING_KEY = "item.updated";
    public static final String ITEM_DELETED_ROUTING_KEY = "item.deleted";
    public static final String ITEM_QUERIED_ROUTING_KEY = "item.queried";
    @Bean
    public DirectExchange itemExchange() {
        return new DirectExchange(ITEM_EXCHANGE);
    }

    @Bean
    public Queue itemCreatedQueue() {
        return new Queue(ITEM_CREATED_QUEUE, true);
    }

    @Bean
    public Queue itemUpdatedQueue() {
        return new Queue(ITEM_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue itemDeletedQueue() {
        return new Queue(ITEM_DELETED_QUEUE, true);
    }
    @Bean
    public Queue itemQueriedQueue() {
        return new Queue(ITEM_QUERIED_QUEUE, true);
    }

    @Bean
    public Binding bindingCreated(@Qualifier("itemCreatedQueue")Queue itemCreatedQueue, DirectExchange itemExchange) {
        return BindingBuilder.bind(itemCreatedQueue).to(itemExchange).with(ITEM_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingUpdated(@Qualifier("itemUpdatedQueue")Queue itemUpdatedQueue, DirectExchange itemExchange) {
        return BindingBuilder.bind(itemUpdatedQueue).to(itemExchange).with(ITEM_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDeleted(@Qualifier("itemDeletedQueue")Queue itemDeletedQueue, DirectExchange itemExchange) {
        return BindingBuilder.bind(itemDeletedQueue).to(itemExchange).with(ITEM_DELETED_ROUTING_KEY);
    }
    @Bean
    public Binding bindingQueried(@Qualifier("itemQueriedQueue")Queue itemQueriedQueue, DirectExchange itemExchange) {
        return BindingBuilder.bind(itemQueriedQueue).to(itemExchange).with(ITEM_QUERIED_ROUTING_KEY);
    }
}
