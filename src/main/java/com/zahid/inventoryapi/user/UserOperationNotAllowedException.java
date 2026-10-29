package com.zahid.inventoryapi.user;

public class UserOperationNotAllowedException extends RuntimeException {
    
    public UserOperationNotAllowedException(String message) {
        super(message);
    }
}