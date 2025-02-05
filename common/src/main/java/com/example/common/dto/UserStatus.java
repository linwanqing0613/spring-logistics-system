package com.example.common.dto;

public enum UserStatus {
    ACTIVE, INACTIVE;
    public static boolean isValidStatus(String status) {
        try {
            UserStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
