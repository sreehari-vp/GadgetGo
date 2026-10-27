package com.gadgetgo.customer.service;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Email or password is incorrect."); }
}
