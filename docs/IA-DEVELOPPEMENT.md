# Utiliser les outils d'IA dans le développement

Le poste demande « une connaissance des outils d'IA dans le cadre du développement d'applications ».
Ce document propose une pratique concrète sur SecuTrack, avec les règles à respecter dans une équipe
liée à la cybersécurité.

## 1. Les outils

| Outil | Usage principal |
|---|---|
| **GitHub Copilot** (VS Code, IntelliJ) | Complétion de code, chat dans l'éditeur, revue de PR |
| **Claude Code** | Agent en ligne de commande : lit le projet, modifie plusieurs fichiers, lance les tests |
| **Assistants conversationnels** (Claude, ChatGPT…) | Expliquer du code, concevoir, rédiger de la documentation |
| **JetBrains AI Assistant** | Intégré à IntelliJ (Java, Spring) |

L'outil utilisé par l'équipe dépend des choix de l'organisation : c'est une question à poser le premier jour.

## 2. Les règles d'or

1. **Jamais de donnée sensible dans un outil non approuvé** : pas de secret, de mot de passe, de jeton,
   de donnée client, ni de code interne dans un service externe non validé par l'organisation.
2. **Tu restes responsable du code** : tout ce que l'IA produit est relu, compris et testé comme le code
   d'un collègue. Si tu ne peux pas l'expliquer en revue, tu ne le commites pas.
3. **Les tests sont l'arbitre** : un code généré n'est accepté que si la CI est verte.
4. **Méfiance sur la sécurité** : l'IA peut proposer du code vulnérable (requête SQL concaténée,
   droits vérifiés seulement côté client, CSRF désactivé sans raison). Relis ces points en priorité.
5. **Méfiance sur les versions** : l'IA mélange souvent les versions (`javax` au lieu de `jakarta`,
   `WebSecurityConfigurerAdapter` supprimé depuis Spring Security 6). Vérifie dans la documentation officielle.

## 3. Exercices sur SecuTrack

| # | Tâche | Exemple de demande | À vérifier |
|---|---|---|---|
| IA-1 | Comprendre du code | « Explique ligne par ligne `TicketSpecifications.avecFiltre` et le SQL généré » | Comparer avec `show-sql: true` |
| IA-2 | Générer des tests | « Écris des tests JUnit pour `UtilisateurService.changerRole`, y compris le cas où un admin retire son propre rôle » | Les tests échouent-ils si on casse la règle ? |
| IA-3 | Revue de code | « Fais une revue de sécurité de `SecuriteConfig` et `TicketService` » | Chaque remarque est-elle fondée ? |
| IA-4 | Implémenter une story | « Implémente SEC-2 (filtre créés par moi) côté Spring et Angular, avec les tests » | Lire tout le diff avant de committer |
| IA-5 | Documentation | « Rédige la section README de la fonctionnalité SEC-3 » | Exactitude par rapport au code |
| IA-6 | Diagnostic | « Voici l'erreur de `./mvnw verify` : <trace>. Quelle est la cause ? » | Comprendre la cause, pas seulement le correctif |
| IA-7 | Migration | « Liste les changements nécessaires pour passer SecuTrack à Spring Boot 4.1 » | Comparer avec le guide officiel de migration |

### Exercice guidé : IA-2

1. Crée la branche `test/utilisateur-service`.
2. Demande les tests à l'outil d'IA, en lui donnant `UtilisateurService.java` et un test existant
   comme modèle de style (`TicketServiceTest.java`).
3. Lance `./mvnw test`. Corrige ce qui ne compile pas.
4. **Test de mutation manuel** : supprime temporairement la vérification « propre rôle » dans
   `changerRole`. Au moins un test doit échouer. Sinon, les tests générés ne protègent rien.
5. Remets le code, commite (`test(utilisateur): couvre changerRole`), ouvre la PR et mentionne
   dans la description que les tests ont été générés avec assistance puis vérifiés.

## 4. Une compétence à montrer

En entrevue ou en équipe, la valeur n'est pas « j'utilise l'IA », mais :
- je sais **découper** une tâche pour que l'IA soit efficace ;
- je **vérifie** systématiquement (tests, documentation officielle, revue) ;
- je respecte les **règles de confidentialité** de l'organisation.
