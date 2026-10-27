package com.gadgetgo.booking.service;

public class InvalidOfferStateException extends RuntimeException {
    public InvalidOfferStateException(String message) {
        super(message);
    }
}
