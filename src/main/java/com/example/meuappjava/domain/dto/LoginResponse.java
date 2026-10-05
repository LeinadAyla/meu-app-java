package com.example.meuappjava.domain.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
