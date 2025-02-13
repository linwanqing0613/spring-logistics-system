package com.example.common.dto;

public enum ModelName {
    USER("USR-"),
    ITEM("ITEM-"),
    ORDER("ORD-");

    private final String prefix;

    ModelName(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}