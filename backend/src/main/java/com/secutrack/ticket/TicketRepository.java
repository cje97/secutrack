package com.secutrack.ticket;

import com.secutrack.ticket.dto.CompteParSeverite;
import com.secutrack.ticket.dto.CompteParStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des tickets. Spring Data JPA génère l'implémentation au démarrage.
 *
 * JpaRepository          : CRUD de base (save, findById, findAll, deleteById...) + pagination.
 * JpaSpecificationExecutor : recherche avec des critères construits dynamiquement (TicketSpecifications).
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    /**
     * Recherche filtrée et paginée.
     *
     * @EntityGraph : charge createur et assigne DANS LA MÊME requête SQL (jointure).
     * Sans lui : 1 requête pour les 10 tickets de la page, puis 1 requête par ticket pour son
     * créateur, et encore 1 pour son assigné = jusqu'à 21 requêtes. C'est le fameux "problème N+1".
     */
    @Override
    @EntityGraph(attributePaths = {"createur", "assigne"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);

    /** Même optimisation pour le détail d'un ticket. */
    @Override
    @EntityGraph(attributePaths = {"createur", "assigne"})
    Optional<Ticket> findById(Long id);

    /**
     * @Query : requête écrite à la main en JPQL. Le JPQL manipule des ENTITÉS et leurs champs
     * (Ticket t, t.statut), pas des tables et des colonnes.
     * "SELECT new ..." construit directement un record pour chaque ligne du résultat (projection).
     */
    @Query("SELECT new com.secutrack.ticket.dto.CompteParStatut(t.statut, COUNT(t)) "
         + "FROM Ticket t GROUP BY t.statut")
    List<CompteParStatut> compterParStatut();

    /** @Param("exclus") relie le paramètre Java au paramètre nommé :exclus de la requête. */
    @Query("SELECT new com.secutrack.ticket.dto.CompteParSeverite(t.severite, COUNT(t)) "
         + "FROM Ticket t WHERE t.statut NOT IN :exclus GROUP BY t.severite")
    List<CompteParSeverite> compterParSeverite(@Param("exclus") Collection<Statut> exclus);

    /**
     * Requête dérivée : Spring lit le nom de la méthode.
     * countBy + Assigne.Courriel + And + Statut + NotIn
     * → SELECT COUNT(*) FROM tickets t JOIN utilisateurs u ON t.assigne_id = u.id
     *   WHERE u.courriel = ? AND t.statut NOT IN (?, ?)
     */
    long countByAssigneCourrielAndStatutNotIn(String courriel, Collection<Statut> exclus);
}
