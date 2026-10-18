# Tester l'API

Trois outils, du plus visuel au plus automatisé.

## 1. Swagger UI (dans le navigateur)

http://localhost:8080/swagger-ui.html : appelle `POST /api/auth/login`, copie le `token`, clique sur
**Authorize** et colle-le. Tous les endpoints deviennent testables.

## 2. Postman (collection fournie)

1. Postman → **Import** → sélectionne les deux fichiers du dossier `docs/postman/` :
   - `SecuTrack.postman_collection.json` : toutes les requêtes, avec des tests automatiques
   - `SecuTrack-docker.postman_environment.json` : environnement pour l'application lancée avec Docker (port 8081)
2. Sans environnement sélectionné, la collection vise `http://localhost:8080` (mode développement).
3. Lance **1. Authentification → Login ADMIN** : le jeton est enregistré automatiquement dans `{{token}}`.
4. Exécute les dossiers dans l'ordre, ou toute la collection avec **Run** (Collection Runner) :
   chaque requête vérifie son code HTTP et affiche ✅ ou ❌.

| Dossier | Contenu |
|---|---|
| 1. Authentification | Login ADMIN, inscription d'un compte aléatoire, profil |
| 2. Tickets | Recherche, statistiques, création (l'id est mémorisé dans `{{ticketId}}`), détail, modification, statut, assignation |
| 3. Cas d'erreur | 401, 400, 404, 409, puis bascule en USER pour obtenir un 403 |
| 4. Nettoyage | Reconnexion ADMIN et suppression du ticket de test |

**Exercice** : ajoute une requête « 403 - un USER passe un ticket à RESOLU » avec son test
`pm.response.to.have.status(403)`, puis exporte la collection et commite-la.

## 3. curl (en ligne de commande)

```bash
# Connexion et récupération du jeton (nécessite jq)
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"courriel":"admin@secutrack.local","motDePasse":"Demo1234!"}' | jq -r .token)

# Requête authentifiée
curl -s localhost:8080/api/tickets/statistiques -H "Authorization: Bearer $TOKEN" | jq

# Observer une erreur 403 avec un compte USER
USER_TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"courriel":"analyste@secutrack.local","motDePasse":"Demo1234!"}' | jq -r .token)
curl -i -X DELETE localhost:8080/api/tickets/1 -H "Authorization: Bearer $USER_TOKEN"
```

## Lire un JWT

Colle un jeton sur https://jwt.io : tu vois son contenu (`sub`, `roles`, `exp`) sans connaître la clé.
C'est la preuve qu'un JWT est **signé, pas chiffré** : on n'y met jamais d'information secrète.
