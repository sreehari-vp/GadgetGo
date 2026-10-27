package com.gadgetgo.booking.service;

public class OfferNotFoundException extends RuntimeException {
    public OfferNotFoundException() {
        super("The requested assignment offer was not found.");
    }
}
