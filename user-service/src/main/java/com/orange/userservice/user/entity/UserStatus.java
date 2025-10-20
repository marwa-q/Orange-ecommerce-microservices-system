package com.orange.userservice.user.entity;

public enum UserStatus {
    ACTIVE,
    BLOCKED;

    public boolean isBlocked(){
        return this == BLOCKED;
    }
}
