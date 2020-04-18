package com.bombsquad.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bombsquad.model.{Game, GameList, GameRequest, User}
import org.mongodb.scala.bson.ObjectId

import scala.concurrent.Future

package object GameProtocol {

  sealed trait Command

  final case class SignupUserCommand(user: User, replyTo: ActorRef[Future[User]]) extends Command

  final case class StartNewGameCommand(username: String, gameRequest: GameRequest, replyTo: ActorRef[Future[String]]) extends Command

  final case class PauseGameCommand(username: String, gameId: String, replyTo: ActorRef[Future[String]]) extends Command

  final case class CancelGameCommand(username: String, gameId: String, replyTo: ActorRef[Future[String]]) extends Command

  final case class FlagCellCommand(username: String, gameId: String, rows: Int, cols: Int, replyTo: ActorRef[Future[String]]) extends Command

  final case class UncoverCellCommand(username: String, gameId: String, rows: Int, cols: Int, replyTo: ActorRef[Future[String]]) extends Command

  final case class ListGamesForCommand(username: String, replyTo: ActorRef[Future[GameList]]) extends Command

  final case class GameStateCommand(username: String, gameId: String, replyTo: ActorRef[Future[Game]]) extends Command

}

trait GameActor {
  this: GameService =>

  val behavior: Behavior[GameProtocol.Command] = Behaviors.receiveMessage {
    case GameProtocol.SignupUserCommand(user, replyTo) =>
      replyTo ! signupUser(user)
      Behaviors.same
    case GameProtocol.StartNewGameCommand(username, gameRequest, replyTo) =>
      replyTo ! startNewGame(username, gameRequest)
      Behaviors.same
    case GameProtocol.PauseGameCommand(username, gameId, replyTo) =>
      replyTo ! pauseGame(username, new ObjectId(gameId))
      Behaviors.same
    case GameProtocol.CancelGameCommand(username, gameId, replyTo) =>
      replyTo ! cancelGame(username, new ObjectId(gameId))
      Behaviors.same
    case GameProtocol.FlagCellCommand(username, gameId, row, col, replyTo) =>
      replyTo ! flagCell(username, new ObjectId(gameId), row, col)
      Behaviors.same
    case GameProtocol.UncoverCellCommand(username, gameId, row, col, replyTo) =>
      replyTo ! unCoverCell(username, new ObjectId(gameId), row, col)
      Behaviors.same
    case GameProtocol.ListGamesForCommand(username, replyTo) =>
      replyTo ! listGamesFor(username)
      Behaviors.same
    case GameProtocol.GameStateCommand(username, gameId, replyTo) =>
      replyTo ! gameState(username, new ObjectId(gameId))
      Behaviors.same
  }
}