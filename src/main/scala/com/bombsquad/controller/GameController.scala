package com.bombsquad.controller

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Forbidden, InternalServerError}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, pathPrefix, post, _}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.util.Timeout
import com.bombsquad.AppConf
import com.bombsquad.JsonFormats._
import com.bombsquad.exception.GameDoesNotBelongToUserException
import com.bombsquad.model.{Game, GameList, GameRequest, User}
import com.bombsquad.service.GameProtocol
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class GameController(delegate: ActorRef[GameProtocol.Command])(implicit val system: ActorSystem[_]) extends LazyLogging {

  private implicit val timeout: Timeout = Timeout.create(AppConf.routesAskTimeout)

  implicit def customExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: GameDoesNotBelongToUserException =>
        extractUri { uri =>
          logger.warn(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(Forbidden))
        }
      case e: IllegalArgumentException => {
        extractUri { uri =>
          logger.warn(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(BadRequest))
        }
      }
      case e: Throwable => {
        extractUri { uri =>
          logger.warn(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(InternalServerError))
        }
      }
    }

  val routes: Route = pathPrefix("bombsquad") {
    path("users") {
      //  Signup user:
      //  POST /bombsquad/users
      post {
        entity(as[User]) { user =>
          logger.debug(s"Will create user $user")
          onSuccess(signupUser(user))(complete(StatusCodes.Created, _))
        }
      }
    } ~
      path("users" / Segment) { username =>
        concat(
          // Start new game:
          // POST /bombsquad/users/{username}/games
          post {
            entity(as[GameRequest]) { gameRequest =>
              logger.debug(s"Will create game $gameRequest")
              onSuccess(startNewGame(username, gameRequest))(complete(StatusCodes.Created, _))
            }
          }
          ,
          // List games for
          // GET /bombsquad/users/{username}/games
          get {
            onSuccess(listGamesFor(username))(complete(StatusCodes.OK, _))
          }
        )
      } ~
      path("users" / Segment / "games" / Segment) { (username, gameId) =>
        concat(
          // Game state
          // GET /bombsquad/users/{username}/games/{gameId}
          get {
            logger.debug(s"Will get game state for user $username and game $gameId")
            onSuccess(gameState(username, gameId))(complete(StatusCodes.OK, _))
          },
          // Pause game:
          // PUT /bombsquad/users/{username}/games/{gameId}/pause
          put {
            logger.debug(s"Will pause game $gameId for user $username")
            onSuccess(pauseGame(username, gameId))(complete(StatusCodes.OK, _))
          },
          // Cancel game
          // PUT /bombsquad/users/{username}/games/{gameId}/cancel
          put {
            logger.debug(s"Will cancel game $gameId for user $username")
            onSuccess(cancelGame(username, gameId))(complete(StatusCodes.OK, _))
          }
        )
      } ~
      // Flag cell
      // PUT /bombsquad/users/{username}/games/{gameId}/flag?row={row}&col={col}
      path("users" / Segment / "games" / Segment / "flag") { (username, gameId) =>
        parameters('row.as[Int], 'col.as[Int]) { (row, col) =>
          put {
            logger.debug(s"Will flag/unflag cell row $row, col $col, for game $gameId and user $username")
            onSuccess(flagCell(username, gameId, row, col))(complete(StatusCodes.OK, _))
          }
        }
      } ~
      // Uncover cell
      // PUT /bombsquad/users/{username}/games/{gameId}/uncover?row={row}&col={col}
      path("users" / Segment / "games" / Segment / "uncover") { (username, gameId) =>
        parameters('row.as[Int], 'col.as[Int]) { (row, col) =>
          put {
            logger.debug(s"Will uncover cell row $row, col $col, for game $gameId and user $username")
            onSuccess(uncoverCell(username, gameId, row, col))(complete(StatusCodes.OK, _))
          }
        }
      }
  }

  def signupUser(user: User): Future[User] =
    delegate ? (GameProtocol.SignupUserCommand(user, _))

  def startNewGame(username: String, gameReq: GameRequest): Future[String] =
    delegate ? (GameProtocol.StartNewGameCommand(username, gameReq.rows, gameReq.cols, gameReq.bombs, _))

  def pauseGame(username: String, gameId: String): Future[String] =
    delegate ? (GameProtocol.PauseGameCommand(username, gameId, _))

  def cancelGame(username: String, gameId: String): Future[String] =
    delegate ? (GameProtocol.CancelGameCommand(username, gameId, _))

  def flagCell(username: String, gameId: String, row: Int, col: Int): Future[String] =
    delegate ? (GameProtocol.FlagCellCommand(username, gameId, row, col, _))

  def uncoverCell(username: String, gameId: String, row: Int, col: Int): Future[String] =
    delegate ? (GameProtocol.UncoverCellCommand(username, gameId, row, col, _))

  def listGamesFor(username: String): Future[GameList] =
    delegate ? (GameProtocol.ListGamesForCommand(username, _))

  def gameState(username: String, gameId: String): Future[Game] =
    delegate ? (GameProtocol.GameStateCommand(username, gameId, _))
}