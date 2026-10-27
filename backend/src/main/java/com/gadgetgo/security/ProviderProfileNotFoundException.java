package com.gadgetgo.security;

public class ProviderProfileNotFoundException extends RuntimeException {
    public ProviderProfileNotFoundException() { super("The provider profile was not found."); }
}
