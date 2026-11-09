# Instructions aux candidat-e-s

- Finaliser/sécuriser l'authentification
- Faire/finir et sécuriser les routes pour voir son client, voir ses comptes/ses users 
- Permettre de se connecter avec un client pour voir ses données
- Pour la route Account, permet de modifier "uniquement" le nom du compte (pas le montant)

# Description du projet

## Base de données

Le projet inclut une base "sqlite.test.db" qui s'initialise au lancement de l'appli (voir DataInitializationConfig.java)

## API REST (java 21)

* http://localhost:8080/api/auth/* → connexion utilisateur, gestion de JWT
* http://localhost:8080/api/users/* → gestion d'utilisateurs (attributs : id, username, password, clientId)
* http://localhost:8080/api/accounts/* → gestion de comptes (attributs : id, name, amount, clientId)
* http://localhost:8080/api/clients/* → gestion de clients (attributs : id, legalEntity)