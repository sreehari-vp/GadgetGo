package com.gadgetgo.catalog.service;

public class CatalogResourceNotFoundException extends RuntimeException {

    public CatalogResourceNotFoundException() {
        super("The requested repair service was not found.");
    }
}
