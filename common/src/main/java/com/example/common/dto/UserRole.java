package com.example.common.dto;

public enum UserRole {
    CUSTOMER, COURIER, ADMIN;
    public static boolean isValidRole(String role) {
        try {
            UserRole.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

