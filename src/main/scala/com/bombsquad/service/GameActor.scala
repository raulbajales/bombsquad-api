package com.bombsquad.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bombsquad.model.{Game, GameList, User}
import org.mongodb.scala.bson.ObjectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

package object GameProtocol {

  sealed trait Command

  final case class SignupUserCommand(user: User, replyTo: ActorRef[Try[User]]) extends Command

  final case class StartNewGameCommand(username: String, rows: Int, cols: Int, bombs: Int, replyTo: ActorRef[Try[String]]) extends Command

  final case class PauseGameCommand(username: String, gameId: String, replyTo: ActorRef[Try[String]]) extends Command

  final case class CancelGameCommand(username: String, gameId: String, replyTo: ActorRef[Try[String]]) extends Command

  final case class FlagCellCommand(username: String, gameId: String, rows: Int, cols: Int, replyTo: ActorRef[Try[String]]) extends Command

  final case class UncoverCellCommand(username: String, gameId: String, rows: Int, cols: Int, replyTo: ActorRef[Try[String]]) extends Command

  final case class ListGamesForCommand(username: String, replyTo: ActorRef[Try[GameList]]) extends Command

  final case class GameStateCommand(username: String, gameId: String, replyTo: ActorRef[Try[Game]]) extends Command
}

trait GameActor {
  this: GameService =>

  val behavior: Behavior[GameProtocol.Command] = Behaviors.receiveMessage {
    case GameProtocol.SignupUserCommand(user, replyTo) =>
      signupUser(user).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.StartNewGameCommand(username, rows, cols, bombs, replyTo) =>
      startNewGame(username, rows, cols, bombs).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.PauseGameCommand(username, gameId, replyTo) =>
      pauseGame(username, new ObjectId(gameId)).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.CancelGameCommand(username, gameId, replyTo) =>
      cancelGame(username, new ObjectId(gameId)).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.FlagCellCommand(username, gameId, row, col, replyTo) =>
      flagCell(username, new ObjectId(gameId), row, col).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.UncoverCellCommand(username, gameId, row, col, replyTo) =>
      unCoverCell(username, new ObjectId(gameId), row, col).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.ListGamesForCommand(username, replyTo) =>
      listGamesFor(username).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
    case GameProtocol.GameStateCommand(username, gameId, replyTo) =>
      gameState(username, new ObjectId(gameId)).map(replyTo ! Success(_)).recover {
        case e => replyTo ! Failure(e)
      }
      Behaviors.same
  }
}