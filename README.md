# SecuTrack — Gestion de tickets de sécurité

Application interne où les employés déclarent et suivent des tickets de sécurité
(incidents, vulnérabilités, demandes d'accès), et où les administrateurs les trient, les assignent
et les résolvent. Le projet reprend la stack du poste Développeur I - Full Stack (Hydro-Québec, concours 187917).

| Couche | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA / Hibernate, Bean Validation |
| Sécurité | Spring Security, JWT signé HS256 (resource server Spring), rôles USER / ADMIN, BCrypt |
| API | REST, ProblemDetail (RFC 9457), pagination / tri / filtres, OpenAPI + Swagger UI |
| Base de données | H2 en développement, PostgreSQL 16 avec Docker |
| Frontend | Angular 22 (standalone, signals, lazy loading), Angular Material, Reactive Forms |
| Tests | JUnit 5, Mockito, MockMvc (backend) · Vitest (frontend) |
| Conteneurs | Dockerfiles multi-stage, docker compose, nginx |
| CI/CD | GitHub Actions (tests + images publiées sur GHCR), TeamCity (Kotlin DSL), Dependabot |

## Documentation

| Document | Contenu |
|---|---|
| **`docs/SecuTrack_Documentation.pdf`** | Documentation technique complète (58 pages) |
| `CONTRIBUTING.md` | Branches, Conventional Commits, pull requests, revue de code |
| `docs/EXERCICES.md` | Exercices Git, TeamCity, backlog et **plan de mise à niveau** |
| `docs/ANALYSE-ET-AGILE.md` | User stories, critères d'acceptation, sprints avec GitHub Projects |
| `docs/TESTER-L-API.md` + `docs/postman/` | Swagger, **collection Postman** avec tests automatiques, curl |
| `docs/BASE-DE-DONNEES.md` + `docs/sql/` | Connexion avec DBeaver / pgAdmin / SQL Workbench/J, requêtes utiles |
| `docs/IA-DEVELOPPEMENT.md` | Outils d'IA en développement : règles et exercices |
| `docs/VIGIE.md` | Versions, fins de support, note de vigie |
| `docs/EXPLOITATION.md` | Déploiement, surveillance, sauvegarde, incidents |
| `docs/GUIDE-UTILISATEUR.md` | Guide pour les utilisateurs finaux |

Le code lui-même est commenté : chaque annotation (`@Entity`, `@Transactional`, `@PreAuthorize`…) et chaque
concept Angular (`signal`, `inject`, `input`…) est expliqué là où il est utilisé.

## Démarrage rapide (Docker)

```bash
docker compose up --build
```

Ouvre http://localhost:8081 et connecte-toi avec un compte de démonstration
(boutons « Admin » et « Utilisateur » sur la page de connexion) :

| Compte | Mot de passe | Rôle |
|---|---|---|
| admin@secutrack.local | Demo1234! | ADMIN |
| analyste@secutrack.local | Demo1234! | USER |
| dev@secutrack.local | Demo1234! | USER |

Swagger UI : http://localhost:8081/swagger-ui.html (bouton **Authorize** : colle le jeton reçu au login).

## Développement (rechargement à chaud)

Prérequis : JDK 21, Node.js 24, Docker Desktop. Maven n'est pas nécessaire grâce à `./mvnw`.

```bash
# Terminal 1 : API sur http://localhost:8080 (base H2 en mémoire + données de démo)
cd backend
./mvnw spring-boot:run

# Terminal 2 : Angular sur http://localhost:4200 (proxy /api → 8080)
cd frontend
npm install
npm start
```

## Tests

```bash
cd backend && ./mvnw verify                     # unitaires + intégration (vrais JWT, vraie sécurité)
cd frontend && npx ng test --watch=false        # Vitest
```

## Fonctionnalités et règles métier

| Action | USER | ADMIN |
|---|---|---|
| S'inscrire, se connecter | ✅ (rôle USER à l'inscription) | ✅ |
| Voir le tableau de bord et tous les tickets | ✅ | ✅ |
| Créer un ticket | ✅ | ✅ |
| Modifier un ticket | ✅ s'il en est le créateur | ✅ |
| Passer un ticket OUVERT ↔ EN_COURS | ✅ créateur ou assigné | ✅ |
| Passer un ticket à RESOLU ou FERME | ❌ | ✅ |
| Assigner un ticket | ❌ | ✅ |
| Supprimer un ticket | ❌ | ✅ |
| Gérer les rôles des utilisateurs | ❌ | ✅ (sauf son propre rôle) |

Un ticket **FERME** n'est plus modifiable. Les règles sont appliquées **dans le backend**
(`TicketService`, `@PreAuthorize`, `SecuriteConfig`) ; le frontend ne fait que masquer les boutons inutiles.

## Architecture

```
secutrack/
├── backend/                     API Spring Boot
│   └── src/main/java/com/secutrack/
│       ├── config/              Sécurité, JWT, CORS, OpenAPI, propriétés
│       ├── auth/                Inscription, connexion, génération des JWT
│       ├── utilisateur/         Entité Utilisateur, rôles, administration
│       ├── ticket/              Entité Ticket, recherche (Specifications), règles métier
│       ├── common/              Exceptions métier + gestion globale des erreurs
│       └── demo/                Données de démonstration au démarrage
├── frontend/                    Application Angular
│   └── src/app/
│       ├── core/                AuthService, intercepteur JWT, guards, erreurs, notifications
│       ├── tickets/             Modèles et services HTTP
│       ├── pages/               Connexion, inscription, tableau de bord, détail, formulaire, utilisateurs
│       └── shared/              Composants réutilisables (dialogue de confirmation)
├── docker-compose.yml           PostgreSQL + backend + frontend
├── .github/                     CI/CD, Dependabot, modèles de PR et d'issues
├── .teamcity/settings.kts       Pipeline TeamCity (Kotlin DSL)
├── teamcity/                    Serveur + agent TeamCity en local
├── CONTRIBUTING.md              Conventions de commits et de pull requests
└── docs/                        Documentation PDF, guides, Postman, SQL, exercices
```

### Parcours d'une requête authentifiée

```
Angular ── authInterceptor ajoute "Authorization: Bearer <JWT>"
   │
   ▼
nginx (/api) ──▶ Spring Security : BearerTokenAuthenticationFilter vérifie la signature et l'expiration
                    │            claim "roles" → ROLE_USER / ROLE_ADMIN
                    ▼
                 Règles d'URL (SecuriteConfig) puis @PreAuthorize
                    ▼
                 Controller (@Valid) → Service (@Transactional, règles métier) → Repository → PostgreSQL
                    ▼
                 DTO JSON ou ProblemDetail (400 / 401 / 403 / 404 / 409)
```

### Points de conception à connaître

- **JWT sans bibliothèque externe** : Spring Security (module resource server) fournit déjà Nimbus pour
  signer (`JwtEncoder`) et vérifier (`JwtDecoder`). Remplacer la clé HS256 par un fournisseur d'identité
  d'entreprise (OIDC) ne demanderait de changer que `JwtConfig`.
- **Jamais d'entité JPA dans les réponses** : uniquement des records DTO (`TicketResponse`, `UtilisateurDto`).
- **Anti N+1** : `@EntityGraph` charge créateur et assigné dans la même requête que les tickets.
- **Verrouillage optimiste** : `@Version` sur `Ticket` → 409 si deux personnes modifient en même temps.
- **Polices auto-hébergées** : aucune dépendance à un CDN externe (utile sur un réseau d'entreprise filtré).

## CI/CD

### GitHub Actions (`.github/workflows/ci-cd.yml`)

À chaque push et pull request : tests backend, tests et build frontend, construction des deux images
Docker. Sur `main`, les images sont publiées dans GitHub Container Registry
(`ghcr.io/<ton-compte>/secutrack-api` et `secutrack-web`).

### TeamCity en local

```bash
cd teamcity
docker compose up -d --build
```

1. Ouvre http://localhost:8111, choisis la base interne, accepte la licence, crée le compte admin.
2. **Agents → Unauthorized** : autorise `secutrack-agent`.
3. **Projects → Create project → From a repository URL** : URL GitHub du dépôt
   (dépôt public, ou token GitHub en mot de passe pour un dépôt privé).
4. Choisis **Import settings from .teamcity/settings.kts** : les builds Backend et Frontend apparaissent.

## Configuration

| Variable | Rôle | Valeur par défaut |
|---|---|---|
| `JWT_SECRET` | Clé de signature des JWT (32 caractères min.) | valeur de développement |
| `DEMO_ACTIF` | Crée les comptes et tickets de démo si la base est vide | `true` |
| `DEMO_MOT_DE_PASSE` | Mot de passe des comptes de démo | `Demo1234!` |
| `POSTGRES_PASSWORD` | Mot de passe PostgreSQL (docker compose) | `secutrack` |

Copie `.env.example` en `.env` pour les personnaliser. Ne commite jamais `.env`.

## État de vérification

- **Frontend** : build de production et 19 tests Vitest passés avant livraison.
- **Backend** : syntaxe Java vérifiée, mais pas encore compilé avec ses dépendances (pas d'accès à
  Maven Central pendant la préparation). La première vérification est `./mvnw verify` chez toi,
  ou le premier run GitHub Actions.

## Notes

- Sous Windows, si `./mvnw` perd son droit d'exécution : `git update-index --chmod=+x backend/mvnw`.
- Spring Boot 3.5 n'a plus de support open source depuis juin 2026 : la migration vers 4.1 fait partie du
  backlog (`docs/EXERCICES.md`).
