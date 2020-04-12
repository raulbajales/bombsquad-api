package com.bombsquad.service

import com.bombsquad.AppConf
import com.bombsquad.exception.GameDoesNotBelongToUserException
import com.bombsquad.model.{Game, User}
import com.bombsquad.repository.{GameRepository, UserRepository}

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
        updateGame(game).map(_.id)
      }
    }
  }

  def pauseGame(username: String,
                gameId: String): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null && !gameId.isBlank, "username is required")
    findGameIdsByUsername(username).flatMap { gameIds =>
      checkGameIds(username, gameIds, gameId)
      findGameById(gameId).flatMap { game =>
        game.pause()
        updateGame(game).map(_.id)
      }
    }
  }

  def cancelGame(username: String,
                 gameId: String): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null && !gameId.isBlank, "username is required")
    findGameIdsByUsername(username).flatMap { gameIds =>
      checkGameIds(username, gameIds, gameId)
      findGameById(gameId).flatMap { game =>
        game.cancel()
        updateGame(game).map(_.id)
      }
    }
  }

  def flagCell(username: String,
               gameId: String,
               row: Int,
               col: Int): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null && !gameId.isBlank, "username is required")
    findGameIdsByUsername(username).flatMap { gameIds =>
      checkGameIds(username, gameIds, gameId)
      findGameById(gameId).flatMap { game =>
        game.flagCell(row, col)
        updateGame(game).map(_.id)
      }
    }
  }

  def unCoverCell(username: String,
                  gameId: String,
                  row: Int,
                  col: Int): Future[String] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null && !gameId.isBlank, "username is required")
    findGameIdsByUsername(username).flatMap { gameIds =>
      checkGameIds(username, gameIds, gameId)
      findGameById(gameId).flatMap { game =>
        game.unCoverCell(row, col)
        updateGame(game).map(_.id)
      }
    }
  }

  def listGamesFor(username: String): Future[Seq[String]] = {
    require(username != null && !username.isBlank, "username is required")
    findGameIdsByUsername(username)
  }

  def gameState(username: String, gameId: String): Future[Game] = {
    require(username != null && !username.isBlank, "username is required")
    require(gameId != null && !gameId.isBlank, "username is required")
    findGameIdsByUsername(username).flatMap { gameIds =>
      checkGameIds(username, gameIds, gameId)
      findGameById(gameId)
    }
  }

  private def checkGameIds(username: String, gameIds: Seq[String], gameId: String): Unit =
    if (!gameIds.contains(gameId))
      throw GameDoesNotBelongToUserException(username, gameId)
}
