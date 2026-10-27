package com.gadgetgo.customer.service;

public class CustomerConflictException extends RuntimeException {
    public CustomerConflictException() { super("An account with this email address already exists."); }
}
