-- =====================================================================
-- Requêtes utiles pour explorer la base SecuTrack (PostgreSQL, via docker compose)
-- Connexion : jdbc:postgresql://localhost:5432/secutrack  |  secutrack / secutrack
-- Les tables sont créées par Hibernate à partir des entités (ddl-auto: update).
-- Note : Hibernate convertit les noms Java en snake_case (nomComplet → nom_complet).
-- =====================================================================

-- 1. Structure : colonnes de la table tickets
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_name = 'tickets'
ORDER BY ordinal_position;

-- 2. Les utilisateurs (le mot de passe est un hash BCrypt : il commence par $2a$)
SELECT id, nom_complet, courriel, role, cree_le, LEFT(mot_de_passe, 7) AS debut_hash
FROM utilisateurs
ORDER BY id;

-- 3. Les tickets avec le nom du créateur et de l'assigné (jointures)
SELECT t.id, t.titre, t.type, t.severite, t.statut,
       c.nom_complet AS createur,
       a.nom_complet AS assigne,
       t.cree_le, t.version
FROM tickets t
JOIN utilisateurs c      ON c.id = t.createur_id
LEFT JOIN utilisateurs a ON a.id = t.assigne_id      -- LEFT : garde les tickets non assignés
ORDER BY t.cree_le DESC;

-- 4. Nombre de tickets par statut (même calcul que GET /api/tickets/statistiques)
SELECT statut, COUNT(*) AS nombre
FROM tickets
GROUP BY statut
ORDER BY statut;

-- 5. Tickets actifs par sévérité (non résolus et non fermés)
SELECT severite, COUNT(*) AS nombre
FROM tickets
WHERE statut NOT IN ('RESOLU', 'FERME')
GROUP BY severite;

-- 6. Charge de travail par personne assignée
SELECT u.nom_complet, COUNT(t.id) AS tickets_actifs
FROM utilisateurs u
LEFT JOIN tickets t ON t.assigne_id = u.id AND t.statut NOT IN ('RESOLU', 'FERME')
GROUP BY u.nom_complet
ORDER BY tickets_actifs DESC;

-- 7. Index existants (créés par @Index dans l'entité Ticket)
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'tickets';

-- 8. Voir le @Version en action : modifie un ticket dans l'application,
--    puis relance cette requête : la colonne version a augmenté de 1.
SELECT id, titre, version, mis_a_jour_le FROM tickets ORDER BY mis_a_jour_le DESC LIMIT 5;

-- 9. Plan d'exécution : la requête utilise-t-elle l'index sur statut ?
EXPLAIN ANALYZE SELECT * FROM tickets WHERE statut = 'OUVERT';

-- 10. ATTENTION : promouvoir un utilisateur ADMIN directement en base
--     (en temps normal, on passe par l'application : page Utilisateurs)
-- UPDATE utilisateurs SET role = 'ADMIN' WHERE courriel = 'dev@secutrack.local';
