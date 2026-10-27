package com.gadgetgo.booking.service;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException() {
        super("The requested repair booking was not found.");
    }
}
