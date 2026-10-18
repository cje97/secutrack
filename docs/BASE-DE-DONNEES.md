# Explorer la base de données

## Avec Docker (PostgreSQL)

Lance `docker compose up`, puis connecte ton client SQL avec ces paramètres :

| Paramètre | Valeur |
|---|---|
| Hôte | `localhost` |
| Port | `5432` |
| Base | `secutrack` |
| Utilisateur | `secutrack` |
| Mot de passe | `secutrack` (ou la valeur de `POSTGRES_PASSWORD` dans `.env`) |
| URL JDBC | `jdbc:postgresql://localhost:5432/secutrack` |

## Quel client utiliser ?

| Outil | Compatible PostgreSQL ? | Remarque |
|---|---|---|
| **DBeaver** (gratuit) | ✅ | Le plus simple : Nouvelle connexion → PostgreSQL, il télécharge le pilote tout seul |
| **pgAdmin** (gratuit) | ✅ | L'outil officiel PostgreSQL |
| **IntelliJ IDEA** (onglet Database) | ✅ | Pratique : la base est à côté du code |
| **SQL Workbench/J** | ✅ | Ajouter le pilote PostgreSQL (fichier .jar) dans *Manage Drivers* |
| **MySQL Workbench** | ❌ | Ne se connecte qu'à MySQL / MariaDB |

Ou directement en ligne de commande, sans rien installer :

```bash
docker compose exec db psql -U secutrack -d secutrack
\dt                        -- lister les tables
\d tickets                 -- structure de la table tickets
SELECT * FROM tickets;
\q                         -- quitter
```

## En développement (H2 en mémoire)

Avec `./mvnw spring-boot:run`, la base H2 n'existe que dans la mémoire de l'application et disparaît à
l'arrêt. Pour travailler avec un client SQL, lance seulement PostgreSQL avec Docker et démarre l'API sur
ce profil :

```bash
docker compose up db -d
cd backend
SPRING_PROFILES_ACTIVE=docker DB_URL=jdbc:postgresql://localhost:5432/secutrack ./mvnw spring-boot:run
```

## Requêtes utiles

Le fichier [`sql/requetes-utiles.sql`](sql/requetes-utiles.sql) contient des requêtes commentées :
structure des tables, jointures tickets / utilisateurs, statistiques, index, plan d'exécution, et
une façon d'observer le verrouillage optimiste (`version`).

## Exercice : relier le code et le SQL

1. Dans `backend/src/main/resources/application.yml`, ajoute sous `spring.jpa` : `show-sql: true`.
2. Redémarre l'API, ouvre le tableau de bord, et lis dans la console le SQL généré par Hibernate.
3. Retrouve la requête qui correspond à `TicketSpecifications` quand tu actives un filtre.
4. Compare avec la requête n° 3 du fichier SQL : c'est la même jointure, écrite à la main.
