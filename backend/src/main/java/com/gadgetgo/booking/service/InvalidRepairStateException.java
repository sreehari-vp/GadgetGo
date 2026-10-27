package com.gadgetgo.booking.service;

public class InvalidRepairStateException extends RuntimeException {
    public InvalidRepairStateException(String message) {
        super(message);
    }
}
