package com.bombsquad.service

import com.bombsquad.model._
import com.bombsquad.repository.{GameRepository, UserRepository}
import org.mongodb.scala.bson.ObjectId
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

import scala.collection.mutable
import scala.concurrent.Future

class GameServiceSpec extends AsyncFlatSpec with Matchers with BeforeAndAfter {

  var ctxt: Context = null

  before {
    ctxt = new Context()
  }

  class Context {
    var calls = mutable.Queue[(String, Any)]()
    var gameIdObj = new ObjectId()
    var username = "john.doe"
    var user = User(username)
    var game = Game(_id = gameIdObj, username = username, board = BoardFactory.createWithSpecificBuriedBombs(5, 5, Set((4, 4))))
    var gameList = GameList(List(gameIdObj.toString))
  }

  trait UserRepositoryStub extends UserRepository {
    def createUser(user: User): Future[User] = {
      ctxt.calls.enqueue(("createUser", user))
      Future(user)
    }

    def findUserByUsername(username: String): Future[User] = {
      val retValue = ctxt.user
      ctxt.calls.enqueue(("findUserByUsername", retValue))
      Future(retValue)
    }
  }

  trait GameRepositoryStub extends GameRepository {
    def createGame(username: String, rows: Int, cols: Int, bombs: Int): Future[Game] = {
      val retValue = ctxt.game
      ctxt.calls.enqueue(("createGame", retValue))
      Future(retValue)
    }

    def findGameById(gameId: ObjectId): Future[Game] = {
      ctxt.calls.enqueue(("findGameById", ctxt.game))
      Future(ctxt.game)
    }

    def updateGame(game: Game): Future[Game] = {
      ctxt.calls.enqueue(("updateGame", game))
      Future(game)
    }

    def findGameIdsByUsername(username: String): Future[GameList] = {
      val retValue = ctxt.gameList
      ctxt.calls.enqueue(("findGameIdsByUsername", retValue))
      Future(retValue)
    }
  }

  object TestGameService extends GameService with UserRepositoryStub with GameRepositoryStub

  "GameService" should "be able to create a user" in {
    TestGameService.createUser(ctxt.user).map { user =>
      ctxt.calls.dequeue() should be(("createUser", user))
      user should not be (null)
      user.username should be(ctxt.username)
    }
  }

  "GameService" should "be able to start a new game and store it" in {
    TestGameService.startNewGame(ctxt.username, 5, 5, 0).map { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      gameId should be(ctxt.gameIdObj.toString)
    }
  }

  "GameService" should "be able to pause a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, 5, 5, 0).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.pauseGame(ctxt.username, new ObjectId(gameId)).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be (Paused.name)
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to cancel a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, 5, 5, 0).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.cancelGame(ctxt.username, new ObjectId(gameId)).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be (Cancelled.name)
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to flag a cell in a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, 5, 5, 0).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.flagCell(ctxt.username, new ObjectId(gameId), 3, 3).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be (Running.name)
        ctxt.game.board.cellAt(3, 3) match {
          case Some(cell) => cell.flagged should be (true)
        }
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to uncover a cell in a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, 5, 5, 0).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.unCoverCell(ctxt.username, new ObjectId(gameId), 3, 3).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be (Running.name)
        ctxt.game.board.cellAt(3, 3) match {
          case Some(cell) => cell.covered should be (false)
        }
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to get game state in a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, 5, 5, 0).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.unCoverCell(ctxt.username, new ObjectId(gameId), 3, 3).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be (Running.name)
        ctxt.game.board.cellAt(3, 3) match {
          case Some(cell) => cell.covered should be (false)
        }
        gameId should be(ctxt.gameIdObj.toString)

        TestGameService.gameState(ctxt.username, new ObjectId(gameId)).flatMap { game =>
          ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
          ctxt.calls.dequeue() should be(("findGameById", ctxt.game))

          game.workflow.currentState should be (Running.name)
          game.board.cellAt(3, 3) match {
            case Some(cell) => cell.covered should be (false)
          }
        }
      }
    }
  }

  "GameService" should "be able to get list og game ids for a user" in {
    TestGameService.listGamesFor(ctxt.username).flatMap { gameList =>
      ctxt.calls.dequeue() should be (("findGameIdsByUsername", ctxt.gameList))
      gameList should be (ctxt.gameList)
    }
  }
}
