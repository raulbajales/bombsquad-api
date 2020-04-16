package com.bombsquad.controller

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.bombsquad.AppConf
import com.bombsquad.JsonFormats._
import com.bombsquad.model.{Game, GameList, GameRequest, User}
import com.bombsquad.service.GameProtocol
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class GameController(delegate: ActorRef[GameProtocol.Command])(implicit val system: ActorSystem[_]) extends LazyLogging {

  private implicit val timeout: Timeout = Timeout.create(AppConf.routesAskTimeout)

  val routes: Route = handleExceptions(CustomExceptionHandler.handler) {
    pathPrefix("bombsquad") {
      concat(
        path("users") {
          //  Signup user:
          //  POST /bombsquad/users
          post {
            entity(as[User]) { user =>
              logger.info(s"Will create user $user")
              onSuccess(signupUser(user))(user => complete(StatusCodes.Created, user))
            }
          }
        },
        path("users" / Segment / "games") { username =>
          concat(
            // Start new game:
            // POST /bombsquad/users/{username}/games
            post {
              entity(as[GameRequest]) { gameRequest =>
                logger.info(s"Will create game $gameRequest")
                onSuccess(startNewGame(username, gameRequest))(complete(StatusCodes.Created, _))
              }
            },
            // List games for
            // GET /bombsquad/users/{username}/games
            get {
              onSuccess(listGamesFor(username))(complete(StatusCodes.OK, _))
            }
          )
        },
        path("users" / Segment / "games" / Segment) { (username, gameId) =>
          // Game state
          // GET /bombsquad/users/{username}/games/{gameId}
          get {
            logger.info(s"Will get game state for user $username and game $gameId")
            onSuccess(gameState(username, gameId))(complete(StatusCodes.OK, _))
          }
        },
        path("users" / Segment / "games" / Segment / "pause") { (username, gameId) =>
          // Pause game:
          // PUT /bombsquad/users/{username}/games/{gameId}/pause
          put {
            logger.info(s"Will pause game $gameId for user $username")
            onSuccess(pauseGame(username, gameId))(complete(StatusCodes.OK, _))
          }
        },
        path("users" / Segment / "games" / Segment / "cancel") { (username, gameId) =>
          // Cancel game
          // PUT /bombsquad/users/{username}/games/{gameId}/cancel
          put {
            logger.info(s"Will cancel game $gameId for user $username")
            onSuccess(cancelGame(username, gameId))(complete(StatusCodes.OK, _))
          }
        },
        path("users" / Segment / "games" / Segment / "flag") { (username, gameId) =>
          // Flag cell
          // PUT /bombsquad/users/{username}/games/{gameId}/flag?row={row}&col={col}
          parameters('row.as[Int], 'col.as[Int]) { (row, col) =>
            put {
              logger.info(s"Will flag/unflag cell row $row, col $col, for game $gameId and user $username")
              onSuccess(flagCell(username, gameId, row, col))(complete(StatusCodes.OK, _))
            }
          }
        },
        path("users" / Segment / "games" / Segment / "uncover") { (username, gameId) =>
          // Uncover cell
          // PUT /bombsquad/users/{username}/games/{gameId}/uncover?row={row}&col={col}
          parameters('row.as[Int], 'col.as[Int]) { (row, col) =>
            put {
              logger.info(s"Will uncover cell row $row, col $col, for game $gameId and user $username")
              onSuccess(uncoverCell(username, gameId, row, col))(complete(StatusCodes.OK, _))
            }
          }
        }
      )
    }
  }

  def signupUser(user: User): Future[Future[User]] = {
    delegate ? (GameProtocol.SignupUserCommand(user, _))
  }

  def startNewGame(username: String, gameReq: GameRequest): Future[Future[String]] =
    delegate ? (GameProtocol.StartNewGameCommand(username, gameReq.rows, gameReq.cols, gameReq.bombs, _))

  def pauseGame(username: String, gameId: String): Future[Future[String]] =
    delegate ? (GameProtocol.PauseGameCommand(username, gameId, _))

  def cancelGame(username: String, gameId: String): Future[Future[String]] =
    delegate ? (GameProtocol.CancelGameCommand(username, gameId, _))

  def flagCell(username: String, gameId: String, row: Int, col: Int): Future[Future[String]] =
    delegate ? (GameProtocol.FlagCellCommand(username, gameId, row, col, _))

  def uncoverCell(username: String, gameId: String, row: Int, col: Int): Future[Future[String]] =
    delegate ? (GameProtocol.UncoverCellCommand(username, gameId, row, col, _))

  def listGamesFor(username: String): Future[Future[GameList]] =
    delegate ? (GameProtocol.ListGamesForCommand(username, _))

  def gameState(username: String, gameId: String): Future[Future[Game]] =
    delegate ? (GameProtocol.GameStateCommand(username, gameId, _))
}