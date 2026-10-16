package com.zahid.inventoryapi.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds) {
}