package com.secutrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test "fumée" : vérifie seulement que toute l'application démarre
 * (configuration valide, beans créés, base H2 initialisée). S'il échoue, rien d'autre ne marchera.
 */
// @SpringBootTest : démarre le contexte Spring COMPLET, comme en vrai
@SpringBootTest
class SecutrackApplicationTests {

    // @Test : méthode de test JUnit 5
    @Test
    void leContexteSpringDemarre() {
    }
}
