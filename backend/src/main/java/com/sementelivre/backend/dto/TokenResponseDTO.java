package com.sementelivre.backend.dto;

public record TokenResponseDTO(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresInSeconds
) {
    public TokenResponseDTO(String accessToken, String refreshToken, Long expiresInSeconds) {
        this(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
