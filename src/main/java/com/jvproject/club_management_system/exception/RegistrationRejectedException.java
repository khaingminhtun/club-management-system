package com.jvproject.club_management_system.exception;

public class RegistrationRejectedException extends RuntimeException {

    public RegistrationRejectedException(String message) {
        super(message);
    }
}