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
$ sbt stage deployHeroku

After that the app is running in:
https://bombsquad-api.herokuapp.com/bombsquad
```

## Some example usages:

### Create a user:
```
$ curl -H 'Content-Type: application/json' -d '{"username":"johndoe"}' https://localhost:8080/bombsquad/users
```
### Start a new game (returns gameId):
```
curl -H 'Content-Type: application/json' -d '{"rows":5, "cols":5, "bombs":5}' http://localhost:8080/bombsquad/users/{username}/games
```
### Uncover a cell (returns gameId):
```
curl -X PUT "http://localhost:8080/bombsquad/users/{username}/games/{gameId}/uncover?row={row}&col={col}"
```
### Flag/Unflag a cell (returns gameId):
```
curl -X PUT "http://localhost:8080/bombsquad/users/{username}/games/{gameId}/flag?row={row}&col={col}"
```
### Get current game state:
```
curl http://localhost:8080/bombsquad/users/{username}/games/{gameId}
```
### Cancel a game:
```
curl -X PUT http://localhost:8080/bombsquad/users/{username}/games/{gameId}/cancel
```
### Pause a game:
```
curl -X PUT http://localhost:8080/bombsquad/users/{username}/games/{gameId}/pause
```
### Get list of game ids for a given username:
```
curl http://localhost:8080/bombsquad/users/{username}/games
```

