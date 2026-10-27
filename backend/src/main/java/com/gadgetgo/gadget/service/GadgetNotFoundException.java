package com.gadgetgo.gadget.service;

public class GadgetNotFoundException extends RuntimeException {
    public GadgetNotFoundException() { super("The requested gadget was not found."); }
}
