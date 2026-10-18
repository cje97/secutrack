# Vigie technologique

Le poste inclut « la vigie de l'évolution des logiciels et applications afin d'influencer les décisions ».
Concrètement : savoir quelles versions on utilise, quand elles ne seront plus supportées, et proposer
les mises à jour au bon moment.

## État des composants de SecuTrack (octobre 2026)

| Composant | Version utilisée | Situation | Action recommandée |
|---|---|---|---|
| Spring Boot | 3.5.16 | Support open source terminé le 30 juin 2026 ; 3.5.16 est la dernière version gratuite | Planifier la migration vers 4.1 (ticket SEC-10) |
| Java | 21 (LTS) | Supporté ; Java 25 est la LTS suivante | Rester sur 21, tester 25 plus tard |
| Angular | 22 | Version actuelle ; la v20 sort de LTS le 28 novembre 2026 | Suivre les mises à jour mineures avec `ng update` |
| PostgreSQL | 16 | Supporté | Rien à court terme |
| Node.js | 24 (LTS) | Exigé par Angular 22 | Rien à court terme |

Ce tableau est un instantané : la vigie consiste justement à le tenir à jour.

## Les outils de vigie du projet

| Outil | Ce qu'il fait |
|---|---|
| **Dependabot** (`.github/dependabot.yml`) | Ouvre chaque semaine des PR de mise à jour (Maven, npm, actions, images Docker). La CI dit si la mise à jour casse quelque chose. |
| **endoflife.date** | Dates de fin de support de presque tous les logiciels |
| **Blogs officiels** | spring.io/blog, blog.angular.dev : nouveautés et calendriers |
| `./mvnw versions:display-dependency-updates` | Liste les dépendances Maven qui ont une version plus récente |
| `npx ng update` | Liste les mises à jour Angular disponibles |
| `npm audit` | Signale les dépendances npm avec des vulnérabilités connues |

## Modèle de note de vigie

Pour influencer une décision, une note courte et factuelle fonctionne mieux qu'un long rapport.

```
Objet : Migration de Spring Boot 3.5 vers 4.1

Constat     : Spring Boot 3.5 ne reçoit plus de correctifs de sécurité gratuits depuis le 30 juin 2026.
Risque      : une faille découverte dans une dépendance Spring ne serait plus corrigée.
Options     : 1) migrer vers 4.1   2) support commercial   3) ne rien faire
Recommandation : option 1. Estimation : 3 à 5 jours (starters renommés, imports de tests, Jackson 3).
Preuve      : migration testée sur SecuTrack, tous les tests verts (lien vers la PR).
```

## Exercice

1. Lance `./mvnw versions:display-dependency-updates` et `npm outdated` dans `frontend/`.
2. Choisis une mise à jour mineure, fais-la dans une branche `chore/...`, vérifie la CI.
3. Rédige une note de vigie pour SEC-10 en t'appuyant sur le guide officiel de migration Spring Boot 4.
