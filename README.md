# bombsquad-api
Akka-HTTP REST API for the BombSquad Game

### Start local server: 
```
$ sbt run
```

### Run tests:
```
$ sbt test
```

### Deploy on heroku:
```
$ sbt herokuPackage/stage deployHeroku

After that the app is running in:
https://bombsquad-api.herokuapp.com/bombsquad
```

## Some example usages (running locally):

### Create a user:
```
$ curl -H 'Content-Type: application/json' -d '{"username":"johndoe", "password":"p@55w0rd"}' http://localhost:8080/bombsquad/users
```
### Login existent user (returns access token in header)
```
$ curl -H 'Content-Type: application/json' -d '{"username":"johndoe", "password":"p@55w0rd"}' http://localhost:8080/bombsquad/users/login
```
### Start a new game (returns gameId):
```
curl -H 'Access-Token: {accessToken}' -H 'Content-Type: application/json' -d '{"rows":5, "cols":5, "bombs":5}' http://localhost:8080/bombsquad/users/{username}/games
```
### Uncover a cell (returns gameId):
```
curl -H 'Access-Token: {accessToken}' -X PUT "http://localhost:8080/bombsquad/users/{username}/games/{gameId}/uncover?row={row}&col={col}"
```
### Flag/Unflag a cell (returns gameId):
```
curl -H 'Access-Token: {accessToken}' -X PUT "http://localhost:8080/bombsquad/users/{username}/games/{gameId}/flag?row={row}&col={col}"
```
### Get current game state:
```
curl -H 'Access-Token: {accessToken}' http://localhost:8080/bombsquad/users/{username}/games/{gameId}
```
### Cancel a game:
```
curl -H 'Access-Token: {accessToken}' -X PUT http://localhost:8080/bombsquad/users/{username}/games/{gameId}/cancel
```
### Pause a game:
```
curl -H 'Access-Token: {accessToken}' -X PUT http://localhost:8080/bombsquad/users/{username}/games/{gameId}/pause
```
### Get list of game ids for a given username:
```
curl -H 'Access-Token: {accessToken}' http://localhost:8080/bombsquad/users/{username}/games
```

