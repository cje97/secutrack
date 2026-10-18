# Exercices SecuTrack

> Documents liés : `CONTRIBUTING.md` (commits et pull requests), `ANALYSE-ET-AGILE.md` (user stories, sprint),
> `TESTER-L-API.md` (Postman), `BASE-DE-DONNEES.md` (SQL), `IA-DEVELOPPEMENT.md`, `VIGIE.md`.

Objectif : pratiquer exactement le quotidien du poste — tickets, branches, pull requests, CI verte,
revue de code, TeamCity — sur une vraie application.

---

## Partie 1 — Mise en place

```bash
cd secutrack
git init
git add .
git commit -m "Initialisation de SecuTrack"
git branch -M main
git remote add origin https://github.com/<ton-compte>/secutrack.git
git push -u origin main
```

Vérifie dans l'onglet **Actions** que les 3 jobs passent. Si le job backend échoue, c'est la vraie
première compilation : lis l'erreur, corrige dans une branche `fix/...`, ouvre une PR.

Active ensuite la protection de `main` : **Settings → Branches → Add rule** →
« Require a pull request before merging » et « Require status checks to pass » (jobs Backend et Frontend).
À partir de là, tout passe par des pull requests, comme en entreprise.

## Partie 2 — Git au quotidien

| # | Exercice | Commandes clés |
|---|---|---|
| G1 | Corriger une coquille dans le README via une branche `docs/...` et une PR | `switch -c`, `commit`, `push -u` |
| G2 | Faire échouer volontairement un test, observer la PR rouge, corriger | — |
| G3 | Provoquer un conflit : deux branches modifient la même ligne d'`application.yml` | `merge`, résolution, `add`, `commit` |
| G4 | Mettre une branche à jour avec `main` | `rebase main`, `push --force-with-lease` |
| G5 | Annuler un commit déjà poussé | `revert` |
| G6 | Récupérer un travail « perdu » après un mauvais `reset --hard` | `reflog` |
| G7 | Mettre de côté un travail en cours pour corriger une urgence | `stash`, `stash pop` |
| G8 | Nettoyer ses commits avant la PR | `rebase -i HEAD~3` (squash) |

## Partie 3 — TeamCity

| # | Exercice |
|---|---|
| T1 | Démarrer TeamCity (README) et faire passer les builds Backend et Frontend au vert |
| T2 | Ouvrir l'onglet **Tests** d'un build backend : retrouver les tests d'intégration |
| T3 | Ajouter une étape `./mvnw -B dependency:tree` dans `.teamcity/settings.kts`, pousser, constater la mise à jour |
| T4 | Ajouter une build configuration « Images Docker » qui dépend des deux autres (snapshot dependency) |
| T5 | Comparer TeamCity et GitHub Actions : noter 3 différences (déclencheurs, agents, artefacts) |

## Partie 4 — Backlog de tickets (une branche + une PR par ticket)

Classés du plus simple au plus ambitieux. Chaque ticket touche backend **et** frontend, avec des tests.

| Ticket | Branche | Description |
|---|---|---|
| SEC-1 | `feature/date-echeance` | Ajouter une date d'échéance optionnelle (`@FutureOrPresent`), affichée en rouge si dépassée |
| SEC-2 | `feature/filtre-createur` | Filtre « Créés par moi » sur le tableau de bord |
| SEC-3 | `feature/commentaires` | Commentaires sur un ticket : entité `Commentaire` (`@ManyToOne` vers Ticket et Utilisateur), endpoint `GET/POST /api/tickets/{id}/commentaires`, fil de discussion sur la page de détail |
| SEC-4 | `feature/historique` | Journal d'audit des changements de statut (qui, quand, ancien → nouveau), avec `@TransactionalEventListener` |
| SEC-5 | `feature/flyway` | Remplacer `ddl-auto: update` par des migrations Flyway et passer en `validate` |
| SEC-6 | `test/testcontainers` | Tests d'intégration sur un vrai PostgreSQL avec Testcontainers et `@ServiceConnection` |
| SEC-7 | `feature/csp-nginx` | Ajouter une Content-Security-Policy dans `nginx.conf` sans casser l'application |
| SEC-8 | `feature/export-csv` | Export CSV des tickets filtrés (ADMIN) |
| SEC-9 | `feature/notifications` | Notification (journal ou courriel) quand un ticket CRITIQUE est créé, avec `@Async` |
| SEC-10 | `chore/spring-boot-4` | Migration vers Spring Boot 4.1 : starters renommés, imports de test, tests verts |

## Partie 5 — Questions à préparer pour le premier jour

- Quelle version de Spring Boot et d'Angular utilise l'application de l'équipe ?
- Quel rôle jouent TeamCity et GitHub Actions dans le pipeline ?
- Comment les utilisateurs s'authentifient-ils (SSO, OIDC, autre) ?
- Où l'application est-elle déployée (quel cloud, Kubernetes) ?
- Quelle convention de branches et de messages de commit l'équipe utilise-t-elle ?

## Partie 6 — Plan de mise à niveau conseillé

| Étape | Contenu | Documents |
|---|---|---|
| 1 | Installer, lancer avec Docker, explorer l'application avec les comptes de démo | README |
| 2 | Pousser sur GitHub, protéger `main`, faire passer la CI | Partie 1, `CONTRIBUTING.md` |
| 3 | Lire le code commenté backend puis frontend, avec le PDF à côté | `SecuTrack_Documentation.pdf` |
| 4 | Tester l'API dans Postman et regarder les données dans un client SQL | `TESTER-L-API.md`, `BASE-DE-DONNEES.md` |
| 5 | Exercices Git G1 à G8 | Partie 2 |
| 6 | TeamCity T1 à T4 | Partie 3 |
| 7 | Créer le tableau GitHub Projects et les issues SEC-1 à SEC-10 | `ANALYSE-ET-AGILE.md` |
| 8 | Sprint 1 : SEC-1, SEC-2, SEC-7 (une PR chacun) | Partie 4 |
| 9 | Sprint 2 : SEC-3, SEC-4 (avec l'exercice d'analyse de SEC-3) | Partie 4 |
| 10 | Exercices IA-1 à IA-3 | `IA-DEVELOPPEMENT.md` |
| 11 | Sprint 3 : SEC-5, SEC-6, puis note de vigie et SEC-10 | `VIGIE.md` |
