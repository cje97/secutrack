package com.secutrack.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de POST /api/auth/login : { "courriel": "...", "motDePasse": "..." } */
public record LoginRequest(
        @NotBlank(message = "Le courriel est obligatoire") String courriel,
        @NotBlank(message = "Le mot de passe est obligatoire") String motDePasse) {
}
