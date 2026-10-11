package com.zahid.inventoryapi.product;

public class InactiveProductException extends RuntimeException {

    public InactiveProductException(String message) {
        super(message);
    }
}
