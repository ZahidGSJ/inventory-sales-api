package com.zahid.inventoryapi.inventory;

public class InvalidMovementQuantityException extends RuntimeException {
    public InvalidMovementQuantityException (String message) {
        super(message);
    }
}