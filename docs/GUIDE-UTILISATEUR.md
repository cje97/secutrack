# Guide utilisateur SecuTrack

Ce guide s'adresse aux personnes qui utilisent l'application (et non aux développeurs). Il sert aussi de
modèle de **matériel de formation**, une activité mentionnée dans la description du poste.

## Se connecter

1. Ouvrir l'adresse de l'application (en local : http://localhost:8081).
2. Saisir son courriel et son mot de passe, puis **Se connecter**.
3. Pas encore de compte ? **Créer un compte** : nom, courriel, mot de passe (8 caractères minimum, dont un chiffre).
   Un nouveau compte a le rôle **Utilisateur**.

La session dure 2 heures. Ensuite, l'application demande de se reconnecter.

## Le tableau de bord

- **Les cartes** du haut : nombre total de tickets, ouverts, en cours, critiques encore actifs, et ceux qui vous sont assignés.
- **La recherche** : tapez un mot du titre ou de la description ; la liste se met à jour toute seule.
- **Les filtres** : statut, sévérité, type, et « Assignés à moi ». « Réinitialiser » les efface.
- **Le tableau** : cliquez sur un en-tête (#, Titre, Statut, Créé le) pour trier ; cliquez sur une ligne pour ouvrir le ticket.

## Déclarer un ticket

1. **Nouveau ticket** (bouton ou menu).
2. Renseigner :
   - **Titre** : court et précis (« Courriel d'hameçonnage imitant la paie »)
   - **Type** : Incident, Vulnérabilité, Demande d'accès, Autre
   - **Sévérité** : Faible, Moyenne, Élevée, Critique
   - **Description** : contexte, systèmes touchés, actions déjà réalisées
3. **Enregistrer** : le ticket est créé avec le statut **Ouvert**.

## Suivre un ticket

Sur la page d'un ticket :

| Action | Qui peut la faire |
|---|---|
| Modifier le contenu | Le créateur et les administrateurs |
| Passer à « En cours » ou revenir à « Ouvert » | Le créateur, la personne assignée, les administrateurs |
| Passer à « Résolu » ou « Fermé » | Les administrateurs |
| Assigner le ticket à quelqu'un | Les administrateurs |
| Supprimer le ticket | Les administrateurs |

Un ticket **Fermé** ne peut plus être modifié.

## Administration (administrateurs)

Menu **Utilisateurs** : liste des comptes et changement de rôle (Utilisateur ↔ Administrateur).
Un administrateur ne peut pas retirer son propre rôle.

## Questions fréquentes

| Question | Réponse |
|---|---|
| Je ne vois pas le bouton Supprimer | Il est réservé aux administrateurs |
| « Résolu » n'apparaît pas dans la liste des statuts | Seul un administrateur peut résoudre un ticket |
| Message « Le ticket a été modifié par quelqu'un d'autre » | Une autre personne l'a modifié en même temps : rechargez la page et recommencez |
| Je suis renvoyé à la page de connexion | La session a expiré : reconnectez-vous |
