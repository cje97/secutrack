package com.secutrack.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corps de POST /api/auth/register.
 *
 * Les annotations de Bean Validation (jakarta.validation) décrivent les règles ;
 * elles sont vérifiées quand le contrôleur met @Valid devant @RequestBody.
 * Si une règle échoue : réponse 400 avec le message de chaque champ en erreur.
 */
public record RegisterRequest(
        // @NotBlank : ni null, ni vide, ni composé uniquement d'espaces ("   " est refusé)
        @NotBlank(message = "Le nom est obligatoire")
        // @Size(max = 100) : longueur maximale, alignée sur @Column(length = 100) de l'entité
        @Size(max = 100, message = "100 caractères maximum")
        String nomComplet,

        @NotBlank(message = "Le courriel est obligatoire")
        // @Email : format d'adresse courriel valide
        @Email(message = "Courriel invalide")
        @Size(max = 255)
        String courriel,

        @NotBlank(message = "Le mot de passe est obligatoire")
        // max = 72 : BCrypt ignore tout ce qui dépasse 72 octets, on le refuse donc explicitement
        @Size(min = 8, max = 72, message = "Entre 8 et 72 caractères")
        // @Pattern : expression régulière ; ".*\\d.*" = contient au moins un chiffre
        @Pattern(regexp = ".*\\d.*", message = "Doit contenir au moins un chiffre")
        String motDePasse) {
}
