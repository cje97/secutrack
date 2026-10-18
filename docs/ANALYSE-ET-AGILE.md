# Analyse des besoins et travail Agile

Le poste demande d'analyser les besoins des utilisateurs, de jouer au besoin le rôle d'analyste
fonctionnel et de travailler en mode Agile. Ce document montre comment SecuTrack a été spécifié,
et comment organiser la suite du projet comme une vraie équipe.

## 1. Du besoin à la user story

**Le besoin exprimé** (tel qu'un client le dirait) :
> « Quand un employé voit quelque chose de suspect, il nous écrit un courriel. On perd des signalements,
> on ne sait pas qui s'en occupe, et on n'a aucune vue d'ensemble. »

**L'analyse** : trois problèmes distincts → trois groupes de fonctionnalités.

| Problème | Fonctionnalité SecuTrack |
|---|---|
| Signalements perdus | Création de tickets typés (incident, vulnérabilité, demande d'accès) |
| Personne ne sait qui s'en occupe | Assignation, statuts, « Assignés à moi » |
| Aucune vue d'ensemble | Tableau de bord, statistiques, filtres |

**Une user story** décrit le besoin du point de vue d'un utilisateur, sans solution technique :

> En tant que **<rôle>**, je veux **<action>**, afin de **<bénéfice>**.

**Les critères d'acceptation** disent quand la story est terminée, au format Étant donné / Quand / Alors.
Ils deviennent directement des tests automatisés.

## 2. Les user stories déjà réalisées

| # | User story | Critères d'acceptation clés | Test qui le prouve |
|---|---|---|---|
| US-1 | En tant qu'employé, je veux créer un compte afin de déclarer des tickets | Un compte créé a le rôle USER ; un courriel déjà utilisé est refusé | `TicketApiIntegrationTest.inscriptionPuisCreationDeTicket` |
| US-2 | En tant qu'employé, je veux déclarer un ticket afin qu'il soit traité | Le ticket naît OUVERT ; le titre est obligatoire | `TicketServiceTest.creer`, `validationRenvoie400...` |
| US-3 | En tant qu'analyste, je veux filtrer et chercher les tickets afin de retrouver rapidement un dossier | Filtres combinables ; recherche dans titre et description | `ticket.service.spec.ts` |
| US-4 | En tant qu'administrateur, je veux assigner un ticket afin que quelqu'un en soit responsable | Seul un ADMIN peut assigner | `@PreAuthorize`, test 403 Postman |
| US-5 | En tant qu'administrateur, je veux être le seul à résoudre ou fermer un ticket afin de valider la qualité du traitement | Un USER reçoit 403 sur RESOLU / FERME | `TicketServiceTest.userNePeutPasResoudre`, `seulLAdminPeutResoudre` |
| US-6 | En tant qu'administrateur, je veux qu'un ticket fermé soit verrouillé afin de garder un historique fiable | Toute modification d'un ticket FERME renvoie 409 | `TicketServiceTest.ticketFermeBloque` |
| US-7 | En tant que responsable, je veux un tableau de bord afin de voir la situation d'un coup d'œil | Total, ouverts, en cours, critiques actifs, assignés à moi | `tableau-de-bord.spec.ts` |

### Exemple complet : US-5

```gherkin
Fonctionnalité: Résolution des tickets réservée aux administrateurs

  Scénario: Un utilisateur tente de résoudre un ticket
    Étant donné que je suis connecté en tant que USER
    Et que le ticket 7 est EN_COURS
    Quand je demande à passer le ticket 7 à RESOLU
    Alors je reçois une réponse 403
    Et le ticket 7 reste EN_COURS

  Scénario: Un administrateur résout un ticket
    Étant donné que je suis connecté en tant que ADMIN
    Quand je demande à passer le ticket 7 à RESOLU
    Alors je reçois une réponse 200
    Et le ticket 7 est RESOLU
```

Comparer avec `TicketApiIntegrationTest.seulLAdminPeutResoudre` : chaque ligne du scénario y a son équivalent.

## 3. Le backlog (prochaines stories)

Chaque ligne se crée comme une **issue GitHub** avec le modèle « User story » (`.github/ISSUE_TEMPLATE`).

| # | User story | Critères d'acceptation | Points |
|---|---|---|---|
| SEC-1 | En tant qu'analyste, je veux une date d'échéance afin de prioriser mon travail | Date optionnelle, pas dans le passé ; affichée en rouge si dépassée | 3 |
| SEC-2 | En tant qu'employé, je veux voir les tickets que j'ai créés afin de suivre mes signalements | Filtre « Créés par moi » combinable avec les autres | 2 |
| SEC-3 | En tant qu'analyste, je veux commenter un ticket afin de documenter l'enquête | Commentaires datés et signés ; impossible sur un ticket FERME | 5 |
| SEC-4 | En tant qu'auditeur, je veux l'historique des statuts afin de savoir qui a fait quoi | Chaque changement enregistre auteur, date, ancien et nouveau statut | 5 |
| SEC-5 | En tant que développeur, je veux des migrations versionnées afin de déployer le schéma sans risque | Flyway ; `ddl-auto: validate` ; l'application démarre sur une base vide | 3 |
| SEC-6 | En tant que développeur, je veux tester sur PostgreSQL afin d'éviter les surprises en production | Testcontainers dans la CI | 3 |
| SEC-7 | En tant qu'équipe sécurité, je veux une Content-Security-Policy afin de limiter les attaques XSS | En-tête CSP dans nginx ; aucune erreur dans la console | 2 |
| SEC-8 | En tant qu'administrateur, je veux exporter les tickets en CSV afin de produire des rapports | Export des tickets filtrés ; réservé ADMIN | 3 |
| SEC-9 | En tant qu'administrateur, je veux être alerté d'un ticket CRITIQUE afin de réagir vite | Notification asynchrone après la création | 3 |
| SEC-10 | En tant qu'équipe, nous voulons migrer vers Spring Boot 4 afin de rester supportés | Tous les tests verts après migration | 8 |

## 4. Organiser un sprint avec GitHub Projects

1. Sur GitHub : **Projects → New project → Board**. Colonnes : `Backlog`, `Sprint`, `En cours`, `En revue`, `Terminé`.
2. Crée les issues SEC-1 à SEC-10 avec le modèle User story, et ajoute-les au projet.
3. Ajoute un champ **Estimation** (nombre) et un champ **Sprint** (itération de 1 semaine).

| Cérémonie Scrum | Ce que tu fais seul sur SecuTrack | Durée |
|---|---|---|
| Planification de sprint | Choisir 2 ou 3 stories (≈ 8 points) et les passer dans `Sprint` | 15 min |
| Mêlée quotidienne | Noter : hier, aujourd'hui, blocages | 5 min |
| Revue de sprint | Démontrer les stories terminées (vidéo ou captures) | 15 min |
| Rétrospective | Ce qui a bien marché, ce qui est à améliorer | 10 min |

**Définition de « terminé »** (Definition of Done) pour SecuTrack :

- [ ] Critères d'acceptation vérifiés
- [ ] Tests automatisés ajoutés (backend et/ou frontend)
- [ ] CI verte (GitHub Actions et TeamCity)
- [ ] Code commenté et documentation mise à jour
- [ ] PR relue et mergée, branche supprimée

## 5. Le rôle d'analyste fonctionnel

Quand tu reçois une demande, avant de coder :

1. **Reformuler** le besoin avec l'utilisateur, et poser la question « pourquoi ? » jusqu'au vrai problème.
2. **Identifier les acteurs** et leurs droits (qui peut faire quoi ?).
3. **Écrire les stories** et leurs critères d'acceptation, les faire valider.
4. **Repérer les impacts** : modèle de données, API, écrans, sécurité, performance.
5. **Découper** en stories livrables en quelques jours.

**Exercice** : pour SEC-3 (commentaires), écris la story complète avec 4 scénarios Étant donné / Quand / Alors
(créer, lister, commentaire vide refusé, ticket fermé), le modèle de données (`Commentaire` avec
ses relations) et la liste des endpoints. Fais-la valider comme si tu étais en atelier avec un client.
