package com.bombsquad.service

import com.bombsquad.controller.{GameDoesNotBelongToUserException, UserCreationException, UserLoginException}
import com.bombsquad.model._
import com.bombsquad.repository.{GameRepository, UserRepository}
import org.mongodb.scala.bson.ObjectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait GameService {
  this: GameRepository with UserRepository =>

  def signupUser(user: User): Future[User] = {
    require(user != null, "user is required")
    createUser(user).recover {
      case e => throw UserCreationException(e.getMessage)
    }
  }

  def loginUser(login: LoginUserRequest): Future[User] = {
    require(login != null, "login user request is required")
    findUserByUsernameAndPassword(login.username, login.password).recover {
      case e => throw UserLoginException(e.getMessage)
    }
  }

  def startNewGame(username: String,
                   gameRequest: GameRequest): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameRequest != null, "gameRequest is required")
    findUserByUsername(username).flatMap { _ =>
      createGame(username, gameRequest).flatMap { game =>
        game.start()
        updateGame(game).map(_._id.toString)
      }
    }
  }

  def pauseGame(username: String,
                gameId: ObjectId): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null, "gameId is required")
    findGameIdsByUsername(username).flatMap { gameList =>
      checkGameIds(username, gameList, gameId)
      findGameById(gameId).flatMap { game =>
        game.pause()
        updateGame(game).map(_._id.toString)
      }
    }
  }

  def cancelGame(username: String,
                 gameId: ObjectId): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null, "gameId is required")
    findGameIdsByUsername(username).flatMap { gameList =>
      checkGameIds(username, gameList, gameId)
      findGameById(gameId).flatMap { game =>
        game.cancel()
        updateGame(game).map(_._id.toString)
      }
    }
  }

  def flagCell(username: String,
               gameId: ObjectId,
               row: Int,
               col: Int): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null, "gameId is required")
    findGameIdsByUsername(username).flatMap { gameList =>
      checkGameIds(username, gameList, gameId)
      findGameById(gameId).flatMap { game =>
        game.flagCell(row, col)
        updateGame(game).map(_._id.toString)
      }
    }
  }

  def unCoverCell(username: String,
                  gameId: ObjectId,
                  row: Int,
                  col: Int): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null, "gameId is required")
    findGameIdsByUsername(username).flatMap { gameList =>
      checkGameIds(username, gameList, gameId)
      findGameById(gameId).flatMap { game =>
        game.unCoverCell(row, col)
        updateGame(game).map(_._id.toString)
      }
    }
  }

  def gameState(username: String, gameId: ObjectId): Future[Game] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null, "gameId is required")
    findGameIdsByUsername(username).flatMap { gameList =>
      checkGameIds(username, gameList, gameId)
      findGameById(gameId)
    }
  }

  def listGamesFor(username: String): Future[GameList] = {
    require(username != null && !username.isBlank, "username is required")
    findGameIdsByUsername(username)
  }

  private def checkGameIds(username: String, gameList: GameList, gameId: ObjectId): Unit =
    if (!gameList.gameIds.contains(gameId.toString))
      throw GameDoesNotBelongToUserException(username, gameId.toString)
}
