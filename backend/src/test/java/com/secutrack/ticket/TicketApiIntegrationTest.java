package com.secutrack.ticket;

import com.jayway.jsonpath.JsonPath;
import com.secutrack.utilisateur.Role;
import com.secutrack.utilisateur.Utilisateur;
import com.secutrack.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de bout en bout : vraie sécurité, vrais JWT, vraie base H2.
 * Chaque test est annulé (rollback) à la fin grâce à @Transactional.
 */
// @SpringBootTest : démarre toute l'application. properties : désactive les données de démo
// pour que chaque test parte d'une base vide et prévisible.
@SpringBootTest(properties = {
        "secutrack.demo.actif=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
// @AutoConfigureMockMvc : fournit MockMvc, qui simule des requêtes HTTP sans lancer de vrai serveur
@AutoConfigureMockMvc
// @Transactional sur un test : chaque test est annulé (rollback) à la fin → base propre pour le suivant
@Transactional
class TicketApiIntegrationTest {

    private static final String MDP = "MotDePasse1";

    // @Autowired : dans un test, on injecte les beans par champ (pas de constructeur à écrire)
    @Autowired MockMvc mockMvc;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired PasswordEncoder passwordEncoder;

    // @BeforeEach : exécutée avant CHAQUE test
    @BeforeEach
    void creerAdmin() {
        utilisateurs.save(new Utilisateur("Admin", "admin@test.local", passwordEncoder.encode(MDP), Role.ADMIN));
    }

    @Test
    void sansJetonLApiRepond401() throws Exception {
        // perform() envoie la requête ; andExpect() vérifie la réponse
        mockMvc.perform(get("/api/tickets")).andExpect(status().isUnauthorized());
    }

    @Test
    void mauvaisMotDePasseRepond401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courriel\":\"admin@test.local\",\"motDePasse\":\"faux12345\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inscriptionPuisCreationDeTicket() throws Exception {
        String jeton = inscrire("bruno@test.local");

        mockMvc.perform(post("/api/tickets").header("Authorization", "Bearer " + jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Phishing\",\"type\":\"INCIDENT\",\"severite\":\"ELEVEE\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                // jsonPath : lit une valeur dans le JSON de la réponse ("$" = la racine)
                .andExpect(jsonPath("$.statut").value("OUVERT"))
                .andExpect(jsonPath("$.createur.courriel").value("bruno@test.local"));

        mockMvc.perform(get("/api/tickets").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void validationRenvoie400AvecLeDetailParChamp() throws Exception {
        String jeton = inscrire("bruno@test.local");

        mockMvc.perform(post("/api/tickets").header("Authorization", "Bearer " + jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"  \",\"type\":\"INCIDENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.titre").exists())
                .andExpect(jsonPath("$.erreurs.severite").exists());
    }

    @Test
    void seulLAdminPeutSupprimer() throws Exception {
        String jetonUser = inscrire("bruno@test.local");
        String jetonAdmin = connecter("admin@test.local");
        long id = creerTicket(jetonUser);

        mockMvc.perform(delete("/api/tickets/" + id).header("Authorization", "Bearer " + jetonUser))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/tickets/" + id).header("Authorization", "Bearer " + jetonAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    void seulLAdminPeutResoudre() throws Exception {
        String jetonUser = inscrire("bruno@test.local");
        String jetonAdmin = connecter("admin@test.local");
        long id = creerTicket(jetonUser);
        String corps = "{\"statut\":\"RESOLU\"}";

        mockMvc.perform(patch("/api/tickets/" + id + "/statut").header("Authorization", "Bearer " + jetonUser)
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/tickets/" + id + "/statut").header("Authorization", "Bearer " + jetonAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESOLU"));
    }

    @Test
    void unUserNeVoitPasLaListeDesUtilisateurs() throws Exception {
        String jetonUser = inscrire("bruno@test.local");
        mockMvc.perform(get("/api/utilisateurs").header("Authorization", "Bearer " + jetonUser))
                .andExpect(status().isForbidden());
    }

    // ---- utilitaires ----

    private String inscrire(String courriel) throws Exception {
        String reponse = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomComplet\":\"Test\",\"courriel\":\"" + courriel + "\",\"motDePasse\":\"" + MDP + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(reponse, "$.token");
    }

    private String connecter(String courriel) throws Exception {
        String reponse = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courriel\":\"" + courriel + "\",\"motDePasse\":\"" + MDP + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(reponse, "$.token");
    }

    private long creerTicket(String jeton) throws Exception {
        String reponse = mockMvc.perform(post("/api/tickets").header("Authorization", "Bearer " + jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Malware\",\"type\":\"INCIDENT\",\"severite\":\"CRITIQUE\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(reponse, "$.id");
        return id.longValue();
    }
}
