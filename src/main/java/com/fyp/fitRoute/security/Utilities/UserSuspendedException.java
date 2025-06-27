package com.fyp.fitRoute.security.Utilities;

public class UserSuspendedException extends RuntimeException {
    public UserSuspendedException(String message) {
        super(message);
    }
}