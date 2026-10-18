# Contribuer à SecuTrack

Ce guide décrit la façon de travailler sur le projet, comme dans une équipe de développement :
un ticket → une branche → des commits propres → une pull request → une revue → un merge.

## 1. Le cycle complet d'un ticket

```bash
# 1. Partir de la dernière version de main
git switch main
git pull

# 2. Créer une branche pour le ticket (numéro de l'issue GitHub dans le nom)
git switch -c feature/12-date-echeance

# 3. Coder, tester, committer par petites étapes
./mvnw verify                          # dans backend/
npx ng test --watch=false              # dans frontend/
git add backend/src/main/java/com/secutrack/ticket/Ticket.java
git commit -m "feat(ticket): ajoute la date d'échéance"

# 4. Pousser la branche
git push -u origin feature/12-date-echeance

# 5. Ouvrir la pull request (sur GitHub, ou en ligne de commande)
gh pr create --fill
```

## 2. Nommer les branches

| Préfixe | Usage | Exemple |
|---|---|---|
| `feature/` | Nouvelle fonctionnalité | `feature/12-date-echeance` |
| `fix/` | Correction d'anomalie | `fix/18-tri-inconnu-500` |
| `test/` | Ajout ou amélioration de tests | `test/21-testcontainers` |
| `docs/` | Documentation uniquement | `docs/connexion-postgresql` |
| `refactor/` | Restructuration sans changement de comportement | `refactor/ticket-mapper` |
| `chore/` | Maintenance : dépendances, configuration, CI | `chore/spring-boot-4` |

Le numéro est celui de l'issue GitHub : GitHub relie alors automatiquement la branche au ticket.

## 3. Écrire de bons commits (Conventional Commits)

Format : `type(portée): description courte au présent`

| Type | Quand | Exemple |
|---|---|---|
| `feat` | Nouvelle fonctionnalité | `feat(ticket): ajoute le filtre « créés par moi »` |
| `fix` | Correction de bug | `fix(auth): renvoie 401 au lieu de 500 si le courriel est vide` |
| `test` | Tests | `test(ticket): vérifie qu'un USER ne peut pas supprimer` |
| `docs` | Documentation | `docs: explique la connexion avec DBeaver` |
| `refactor` | Restructuration | `refactor(ticket): extrait la vérification des droits` |
| `style` | Formatage, sans changement de logique | `style(frontend): applique prettier` |
| `chore` | Maintenance | `chore(deps): met à jour Angular Material` |
| `ci` | Pipelines | `ci: publie les images seulement sur main` |

Règles :

- **Un commit = un changement logique.** Pas de « modifs diverses ».
- **Description courte** (moins de 72 caractères), au présent, sans point final.
- **Un corps facultatif** après une ligne vide pour expliquer le *pourquoi* :

  ```
  fix(ticket): bloque la modification d'un ticket fermé

  Un ticket fermé pouvait encore être réassigné par un ADMIN.
  La vérification est maintenant faite aussi dans assigner().

  Closes #24
  ```

- `Closes #24` dans un commit ou une PR ferme automatiquement l'issue 24 au merge.
- **Jamais de secret** dans un commit (mot de passe, jeton, fichier `.env`). Si ça arrive :
  le changer immédiatement, puis le retirer de l'historique.

## 4. La pull request

### Avant d'ouvrir la PR

- [ ] La branche est à jour avec `main` (`git fetch && git rebase origin/main`)
- [ ] Les tests passent en local (backend et frontend)
- [ ] Le code ajouté est commenté là où ce n'est pas évident
- [ ] Les commits sont propres (au besoin : `git rebase -i origin/main` pour les regrouper)

### Ouvrir la PR

**Sur GitHub** : après le `git push`, un bandeau « Compare & pull request » apparaît sur la page du dépôt.
Le formulaire est pré-rempli par `.github/pull_request_template.md` : complète chaque section.

**En ligne de commande** (GitHub CLI, https://cli.github.com) :

```bash
gh pr create --title "feat(ticket): date d'échéance" --body "Closes #12"
gh pr checks          # état de la CI
gh pr view --web      # ouvrir la PR dans le navigateur
gh pr merge --squash --delete-branch
```

### Ce qui se passe ensuite

1. **GitHub Actions** lance les tests backend, frontend et la construction des images.
2. **TeamCity** (s'il est configuré) lance aussi ses builds.
3. Un **relecteur** commente le code. Pour corriger : nouveaux commits sur la même branche, puis `git push`.
   La PR se met à jour toute seule.
4. Quand la CI est verte et la PR approuvée : **Squash and merge**, puis suppression de la branche.

## 5. Faire une revue de code

Une bonne revue vérifie, dans l'ordre :

1. **Le besoin** : le code fait-il ce que demande le ticket, critères d'acceptation compris ?
2. **La sécurité** : les droits sont-ils vérifiés dans le backend (pas seulement cachés dans Angular) ?
   Des données sensibles sont-elles exposées dans un DTO, un log ou un message d'erreur ?
3. **Les tests** : le nouveau comportement est-il testé, y compris les cas d'erreur (400, 403, 404, 409) ?
4. **La lisibilité** : noms clairs, méthodes courtes, commentaires utiles.
5. **Les détails** : formatage, conventions.

Formule les commentaires comme des questions ou des suggestions :
« Que se passe-t-il si `assigneId` est null ici ? » plutôt que « C'est faux ».

## 6. Protéger la branche main

Dans GitHub : **Settings → Branches → Add branch ruleset** sur `main` :

- Require a pull request before merging (1 approbation)
- Require status checks to pass : `Backend (Maven)` et `Frontend (Angular)`
- Block force pushes

À partir de là, plus personne ne pousse directement sur `main`.
