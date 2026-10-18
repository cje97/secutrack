package com.secutrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Point d'entrée de l'application : c'est la classe qu'on lance (méthode main).
 *
 * IMPORTANT : cette classe doit rester dans le package racine "com.secutrack".
 * Spring ne cherche les classes annotées (@Service, @RestController...) que dans
 * ce package et ses sous-packages (config, auth, ticket...).
 */
// @SpringBootApplication regroupe 3 annotations :
//   - @Configuration            : cette classe peut déclarer des beans (objets gérés par Spring)
//   - @EnableAutoConfiguration  : Spring Boot configure tout seul ce qu'il trouve dans le pom
//                                 (base de données, serveur web Tomcat, Jackson pour le JSON...)
//   - @ComponentScan            : Spring parcourt les packages pour trouver nos classes annotées
@SpringBootApplication
// @ConfigurationPropertiesScan : enregistre automatiquement les classes annotées
// @ConfigurationProperties (ici SecutrackProperties, qui lit le bloc "secutrack:" du YAML)
@ConfigurationPropertiesScan
public class SecutrackApplication {

    public static void main(String[] args) {
        // Démarre Spring : crée tous les beans, puis lance le serveur Tomcat intégré sur le port 8080
        SpringApplication.run(SecutrackApplication.class, args);
    }
}
