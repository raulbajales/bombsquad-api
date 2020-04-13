package com.bombsquad.service

import com.bombsquad.AppConf
import com.bombsquad.exception.GameDoesNotBelongToUserException
import com.bombsquad.model.{Game, GameList, User}
import com.bombsquad.repository.{GameRepository, UserRepository}
import org.mongodb.scala.bson.ObjectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait GameService {
  this: GameRepository with UserRepository =>

  def signupUser(user: User): Future[User] = {
    require(user != null, "user is required")
    createUser(user)
  }

  def startNewGame(username: String,
                   rows: Int = AppConf.defaultRows,
                   cols: Int = AppConf.defaultRows,
                   bombs: Int = AppConf.defaultBombs): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    findUserByUserame(username).flatMap { _ =>
      createGame(username, rows, cols, bombs).flatMap { game =>
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

  def listGamesFor(username: String): Future[GameList] = {
    require(username != null && !username.isBlank, "username is required")
    findGameIdsByUsername(username)
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

  private def checkGameIds(username: String, gameList: GameList, gameId: ObjectId): Unit =
    if (!gameList.gameIds.contains(gameId.toString))
      throw GameDoesNotBelongToUserException(username, gameId.toString)
}
