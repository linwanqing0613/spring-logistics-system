package com.example.common.dto;

public enum RedisKey {
    ITEM_CACHE("ITEM_CACHE:"),
    ITEM_LOCK("ITEM_LOCK:"),
    ORDER("ORD_CACHE:");

    private final String prefix;

    RedisKey(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
