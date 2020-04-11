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
import com.bombsquad.model.User
import com.bombsquad.service.GameProtocol
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

case class GameRequest(username: String, rows: Int, cols: Int, bombs: Int)

class GameController(delegate: ActorRef[GameProtocol.Command])(implicit val system: ActorSystem[_]) extends LazyLogging {

  private implicit val timeout: Timeout = Timeout.create(AppConf.routesAskTimeout)

  def sugnupUser(user: User): Future[User] =
    delegate ? (GameProtocol.SignupUserCommand(user, _))

  def startNewGame(gameReq: GameRequest): Future[String] =
    delegate ? (GameProtocol.StartNewGameCommand(gameReq.username, gameReq.rows, gameReq.cols, gameReq.bombs, _))

  val routes: Route = pathPrefix("bombsquad") {
    path("users") {
      //  Signup user:
      //  POST /bombsquad/users
      post {
        entity(as[User]) { user =>
          logger.debug(s"Will create user $user")
          onSuccess(sugnupUser(user)) {
            complete(StatusCodes.Created, _)
          }
        }
      } ~
        //  Start new game:
        //  POST /bombsquad/users/{userId}/games
        post {
          entity(as[GameRequest]) { gameRequest =>
            logger.debug(s"Will create game $gameRequest")
            onSuccess(startNewGame(gameRequest)) {
              complete(StatusCodes.Created, _)
            }
          }
        }
    }
  }

  // WIP:

  // PauseGameCommand
  // PUT /bombsquad/users/{userId}/games/{gameId}/pause

  // CancelGameCommand
  // PUT /bombsquad/users/{userId}/games/{gameId}/cancel

  // FlagCellCommand
  // PUT /bombsquad/users/{userId}/games/{gameId}/flag?row={row}&col={col}

  // UncoverCellCommand
  // PUT /bombsquad/users/{userId}/games/{gameId}/uncover?row={row}&col={col}

  // ListGamesForCommand
  // GET /bombsquad/users/{userId}/games

  // GameStateCommand
  // GET /bombsquad/users/{userId}/games/{gameId}

}